package com.example.vinilosmobile

import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vinilosmobile.di.DatabaseModule
import com.example.vinilosmobile.di.NetworkModule
import com.example.vinilosmobile.di.RepositoryModule
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import com.example.vinilosmobile.feature.home.MainActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonParser
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** E2E scenarios of HU07: a collector creates an album from the catalog. */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, NetworkModule::class, DatabaseModule::class)
class CreateAlbumEspressoTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @BindValue @JvmField val fixtures = FixtureApiInterceptor()
    @BindValue @JvmField val roleRepository: UserRoleRepository = FakeUserRoleRepository(UserRole.COLLECTOR)

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun prepare() {
        hiltRule.inject()
    }

    @Test fun hu07_visitorCannotCreateAlbums() {
        runBlocking { roleRepository.setRole(UserRole.VISITOR) }
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(allOf(withId(R.id.albumTitle), withText("Buscando América")))
            onView(withId(R.id.addAlbumFab)).check(matches(not(isDisplayed())))
        }
    }

    @Test fun hu07_newAlbumButtonOnlyShowsInCatalog() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(withId(R.id.addAlbumFab))
            onView(withId(R.id.nav_artists)).perform(click())
            eventually { onView(withId(R.id.addAlbumFab)).check(matches(not(isDisplayed()))) }
            onView(withId(R.id.nav_albums)).perform(click())
            waitFor(withId(R.id.addAlbumFab))
        }
    }

    @Test fun hu07_emptyFormMarksEveryFieldAsRequired() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openForm()
            onView(withId(R.id.saveButton)).perform(click())

            onView(withId(R.id.formMessage)).check(matches(withText(R.string.create_album_check_fields)))
            FIELDS.forEach { onView(withId(it)).check(matches(hasFieldError(R.string.field_required))) }
            assertFalse(fixtures.requestedPaths.contains("POST /albums"))
        }
    }

    @Test fun hu07_invalidCoverAndDateAreNotSent() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openForm()
            fillForm(cover = "portada.jpg", date = "31/02/1978")
            onView(withId(R.id.saveButton)).perform(click())

            onView(withId(R.id.coverLayout)).check(matches(hasFieldError(R.string.field_invalid_url)))
            onView(withId(R.id.releaseDateLayout)).check(matches(hasFieldError(R.string.field_invalid_date)))
            onView(withId(R.id.nameLayout)).check(matches(hasFieldError(null)))
            assertFalse(fixtures.requestedPaths.contains("POST /albums"))
        }
    }

    @Test fun hu07_createdAlbumAppearsFirstAndOpensItsDetail() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openForm()
            fillForm()
            onView(withId(R.id.saveButton)).perform(click())

            waitFor(withText(context.getString(R.string.album_created, "Siembra")))
            eventually {
                onView(withId(R.id.albumGrid)).check(matches(atPosition(0, hasDescendant(withText("Siembra")))))
                onView(withId(R.id.albumCount)).check(matches(withText(context.getString(R.string.album_count, 3))))
            }
            val sent = JsonParser.parseString(fixtures.sentAlbum).asJsonObject
            assertEquals("1978-08-01", sent["releaseDate"].asString)
            assertEquals("Salsa", sent["genre"].asString)
            assertEquals("Fania Records", sent["recordLabel"].asString)

            onView(allOf(withId(R.id.albumTitle), withText("Siembra"))).perform(click())
            waitFor(allOf(withId(R.id.detailName), withText("Siembra")))
        }
    }

    @Test fun hu07_serverErrorKeepsTheTypedData() {
        fixtures.responseCodes["POST /albums"] = 500
        ActivityScenario.launch(MainActivity::class.java).use {
            openForm()
            fillForm()
            onView(withId(R.id.saveButton)).perform(click())

            val message = context.getString(R.string.create_album_failed, context.getString(R.string.request_server))
            waitFor(allOf(withId(R.id.formMessage), withText(message)))
            onView(withId(R.id.nameInput)).check(matches(withText("Siembra")))
            onView(withId(R.id.saveButton)).check(matches(isEnabled()))
        }
    }

    @Test fun hu07_cancelGoesBackWithoutSaving() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openForm()
            onView(withId(R.id.nameInput)).perform(replaceText("Siembra"))
            onView(withId(R.id.cancelButton)).perform(click())

            waitFor(withId(R.id.addAlbumFab))
            onView(withId(R.id.albumGrid)).check(matches(not(hasDescendant(withText("Siembra")))))
            assertFalse(fixtures.requestedPaths.contains("POST /albums"))
        }
    }

    private fun openForm() {
        waitFor(allOf(withId(R.id.albumTitle), withText("Buscando América")))
        onView(withId(R.id.addAlbumFab)).perform(click())
        waitFor(allOf(withId(R.id.formMessage), withText(R.string.create_album_required)))
    }

    private fun fillForm(cover: String = "https://example.com/siembra.jpg", date: String = "01/08/1978") {
        onView(withId(R.id.nameInput)).perform(replaceText("Siembra"))
        onView(withId(R.id.coverInput)).perform(replaceText(cover))
        onView(withId(R.id.releaseDateInput)).perform(replaceText(date))
        choose(R.id.genreInput, "Salsa")
        choose(R.id.recordLabelInput, "Fania Records")
        onView(withId(R.id.descriptionInput)).perform(replaceText("Willie Colón y Rubén Blades, 1978"))
    }

    private fun choose(fieldId: Int, option: String) {
        onView(withId(fieldId)).perform(click())
        eventually { onView(withText(option)).inRoot(isPlatformPopup()).perform(click()) }
    }

    private fun waitFor(matcher: Matcher<View>) = eventually { onView(matcher).check(matches(isDisplayed())) }

    private fun eventually(check: () -> Unit) {
        val deadline = System.currentTimeMillis() + 10_000L
        while (true) {
            val failure = try {
                check()
                return
            } catch (error: AssertionError) {
                error
            } catch (error: RuntimeException) {
                error
            }
            if (System.currentTimeMillis() > deadline) throw failure
            Thread.sleep(100)
        }
    }

    private fun hasFieldError(@StringRes message: Int?): Matcher<View> =
        object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("campo con el error $message")
            }

            override fun matchesSafely(item: TextInputLayout): Boolean =
                item.error?.toString() == message?.let(item.context::getString)
        }

    private fun atPosition(position: Int, item: Matcher<View>): Matcher<View> =
        object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("elemento $position que cumple ")
                item.describeTo(description)
            }

            override fun matchesSafely(list: RecyclerView): Boolean =
                list.findViewHolderForAdapterPosition(position)?.itemView?.let(item::matches) == true
        }

    private companion object {
        val FIELDS = listOf(
            R.id.nameLayout, R.id.coverLayout, R.id.releaseDateLayout,
            R.id.genreLayout, R.id.recordLabelLayout, R.id.descriptionLayout,
        )
    }
}
