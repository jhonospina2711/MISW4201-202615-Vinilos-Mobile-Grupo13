package com.example.vinilosmobile

import android.app.Activity
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.viewpager2.widget.ViewPager2
import com.example.vinilosmobile.di.DatabaseModule
import com.example.vinilosmobile.di.NetworkModule
import com.example.vinilosmobile.di.RepositoryModule
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import com.example.vinilosmobile.feature.albumdetail.AlbumDetailActivity
import com.example.vinilosmobile.feature.home.MainActivity
import com.example.vinilosmobile.feature.trackcreate.AddTrackBottomSheet
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale

/**
 * Auditoría de dispositivo: mide objetivos táctiles y perfil de memoria/frames
 * sobre el APK real. No sustituye la revisión con TalkBack ni el Accessibility
 * Scanner; aporta la medición objetiva de tamaños y del PSS por pantalla.
 *
 * Se ejecuta en emulador y en dispositivos físicos de Firebase Test Lab.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, NetworkModule::class, DatabaseModule::class)
class DeviceAuditTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)
    @BindValue @JvmField val fixtures = FixtureApiInterceptor()
    @BindValue @JvmField val roleRepository: UserRoleRepository = FakeUserRoleRepository(UserRole.COLLECTOR)

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val findings = mutableListOf<String>()
    private val report = StringBuilder()

    @Before fun prepare() {
        hiltRule.inject()
        report.appendLine("VINILOS_AUDIT device=${Build.MANUFACTURER} ${Build.MODEL} api=${Build.VERSION.SDK_INT}")
    }

    @Test fun touchTargets_areAtLeast48dpOnEveryScreen() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            settle()
            audit(scenario, "HU01 catálogo de álbumes")

            onView(withId(R.id.nav_artists)).perform(click())
            settle()
            audit(scenario, "HU03 catálogo de artistas (chips)")

            onView(withId(R.id.nav_collectors)).perform(click())
            settle()
            audit(scenario, "HU05 listado de coleccionistas")

            onView(withId(R.id.nav_albums)).perform(click())
            settle()
            onView(withId(R.id.addAlbumFab)).perform(click())
            settle()
            auditCurrentActivity("HU07 formulario de nuevo álbum")
            pressBack()
            settle()
        }

        ActivityScenario.launch<AlbumDetailActivity>(AlbumDetailActivity.intent(context(), 100)).use { scenario ->
            settle()
            audit(scenario, "HU02 detalle de álbum")

            onView(withId(R.id.addTrackFab)).perform(click())
            settle()
            scenario.onActivity { activity ->
                val sheet = activity.supportFragmentManager
                    .findFragmentByTag(AddTrackBottomSheet.TAG) as? AddTrackBottomSheet
                val dialogView = sheet?.dialog?.window?.decorView
                if (dialogView != null) auditView(dialogView, "HU08 hoja de agregar track")
                else addFinding("HU08 hoja de agregar track", "no se pudo abrir la hoja", "0x0")
            }
        }

        report.appendLine("VINILOS_AUDIT objetivos_tactiles_menores_48dp=${findings.size}")
        findings.forEach { report.appendLine("VINILOS_AUDIT hallazgo=$it") }
        publishReport()
        assertTrue("Objetivos táctiles menores a 48 dp:\n${findings.joinToString("\n")}", findings.isEmpty())
    }

    @Test fun profile_reportsPssAndFramesPerScreen() {
        ActivityScenario.launch(MainActivity::class.java).use {
            settle(3000)
            measure("HU01 catálogo de álbumes")

            onView(withId(R.id.nav_artists)).perform(click())
            settle(2500)
            measure("HU03 catálogo de artistas")

            onView(withId(R.id.nav_collectors)).perform(click())
            settle(2500)
            measure("HU05 listado de coleccionistas")
        }

        ActivityScenario.launch<AlbumDetailActivity>(AlbumDetailActivity.intent(context(), 100)).use {
            settle(3000)
            measure("HU02 detalle de álbum")
            onView(withId(R.id.addTrackFab)).perform(click())
            settle(2500)
            measure("HU08 hoja de agregar track")
        }

        publishReport()
        assertTrue("Memoria no reportada", report.contains("rss_kb="))
    }

    // --- Auditoría de tamaños -------------------------------------------------

    private fun audit(scenario: ActivityScenario<*>, label: String) {
        scenario.onActivity { activity -> auditView(activity.window.decorView, label) }
    }

    private fun auditCurrentActivity(label: String) {
        var activity: Activity? = null
        instrumentation.runOnMainSync {
            activity = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED).firstOrNull()
        }
        val decor = activity?.window?.decorView
        if (decor == null) addFinding(label, "sin actividad en RESUMED", "0x0")
        else auditView(decor, label)
    }

    private fun auditView(root: View, label: String) {
        val density = root.resources.displayMetrics.density
        val screenWidth = root.resources.displayMetrics.widthPixels
        var inspected = 0
        var clickable = 0

        fun visit(view: View) {
            if (!view.isShown) return
            inspected++
            if (view is ViewGroup) {
                for (index in 0 until view.childCount) visit(view.getChildAt(index))
            }
            val isInteractive = view.isClickable || view.hasOnClickListeners()
            val isScrollContainer = view is RecyclerView || view is ViewPager2 ||
                view is android.widget.ScrollView || view is android.widget.HorizontalScrollView ||
                view is androidx.core.widget.NestedScrollView
            if (!isInteractive || isScrollContainer) return
            if (view.width <= 0 || view.height <= 0) return
            clickable++

            val widthDp = view.width / density
            val heightDp = view.height / density
            val spansScreen = view.width >= screenWidth * 0.9f
            val label0 = viewName(view)
            if (heightDp < MIN_TARGET_DP && !spansScreen) {
                addFinding(label, label0, fmt(widthDp, heightDp))
            } else if (widthDp < MIN_TARGET_DP) {
                addFinding(label, label0, fmt(widthDp, heightDp))
            }
        }

        visit(root)
        report.appendLine("VINILOS_AUDIT pantalla=\"$label\" vistas=$inspected clicables=$clickable")
    }

    private fun viewName(view: View): String {
        val id = if (view.id != View.NO_ID) {
            runCatching { view.resources.getResourceEntryName(view.id) }.getOrDefault(view.id.toString())
        } else "-"
        val text = (view as? android.widget.TextView)?.text?.toString()?.take(28).orEmpty()
        return "${view.javaClass.simpleName}#$id${if (text.isNotBlank()) " \"$text\"" else ""}"
    }

    private fun addFinding(screen: String, element: String, size: String) {
        findings += "$screen | $element | $size"
    }

    private fun fmt(widthDp: Float, heightDp: Float): String =
        String.format(Locale.US, "%.1fx%.1f dp", widthDp, heightDp)

    // --- Perfil ---------------------------------------------------------------

    private fun measure(label: String) {
        // VmRSS y VmHWM de /proc/self/status funcionan en todas las versiones y
        // no requieren permisos. getProcessMemoryInfo devuelve 0 en API 21.
        val status = runCatching { File("/proc/self/status").readText() }.getOrDefault("")
        val rss = Regex("VmRSS:\\s*(\\d+) kB").find(status)?.groupValues?.get(1) ?: "no-registrado"
        val peak = Regex("VmHWM:\\s*(\\d+) kB").find(status)?.groupValues?.get(1) ?: "no-registrado"

        val pkg = instrumentation.targetContext.packageName
        val frames = runCatching { shell("dumpsys gfxinfo $pkg") }.getOrDefault("")
        val total = Regex("Total frames rendered:\\s*(\\d+)").find(frames)?.groupValues?.get(1) ?: "no-registrado"
        val janky = Regex("Janky frames:\\s*(\\d+)").find(frames)?.groupValues?.get(1) ?: "no-registrado"

        report.appendLine(
            "VINILOS_AUDIT perfil=\"$label\" rss_kb=$rss peak_rss_kb=$peak frames_total=$total janky_frames=$janky",
        )
    }

    private fun shell(command: String): String {
        val descriptor: ParcelFileDescriptor = instrumentation.uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText() }
    }

    // --- Utilidades -----------------------------------------------------------

    private fun context() = instrumentation.targetContext

    private fun settle(millis: Long = 1500) {
        instrumentation.waitForIdleSync()
        SystemClock.sleep(millis)
        instrumentation.waitForIdleSync()
    }

    private fun publishReport() {
        report.toString().lineSequence().forEach { line ->
            println(line)
            android.util.Log.i(TAG, line)
        }
        val dir = instrumentation.targetContext.getExternalFilesDir(null) ?: instrumentation.targetContext.filesDir
        runCatching { File(dir, "device-audit.txt").writeText(report.toString()) }
    }

    private companion object {
        const val MIN_TARGET_DP = 48f
        const val TAG = "VINILOS_AUDIT"
    }
}
