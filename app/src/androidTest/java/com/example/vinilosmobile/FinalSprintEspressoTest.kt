package com.example.vinilosmobile

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager2.widget.ViewPager2
import com.example.vinilosmobile.di.DatabaseModule
import com.example.vinilosmobile.di.NetworkModule
import com.example.vinilosmobile.di.RepositoryModule
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import com.example.vinilosmobile.feature.albumdetail.AlbumDetailActivity
import com.example.vinilosmobile.feature.collectordetail.CollectorDetailActivity
import com.example.vinilosmobile.feature.home.MainActivity
import com.google.gson.JsonParser
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** HTTP fixture exercises Retrofit, repositories and UI for HU05, HU06 and HU08. */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, NetworkModule::class, DatabaseModule::class)
class FinalSprintEspressoTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)
    @BindValue @JvmField val fixtures = FixtureApiInterceptor()
    @BindValue @JvmField val roleRepository: UserRoleRepository = FakeUserRoleRepository(UserRole.VISITOR)

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun prepare() { hiltRule.inject() }

    @Test fun hu05_collectorListOpensHu06DetailAndFavorite() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.nav_collectors)).perform(click())
            waitForCollectorsPage(scenario)
            waitFor(allOf(withId(R.id.collectorName), withText("Carlos Molina"), isCompletelyDisplayed()))
            onView(withId(R.id.collectorList)).check(matches(hasDescendant(withText("1 álbum · 1 artista favorito"))))
            onView(allOf(withId(R.id.collectorName), withText("Carlos Molina"))).perform(click())
            waitFor(allOf(withId(R.id.collectorPhone), withText("3101234567")))
            onView(withId(R.id.collectorEmail)).check(matches(withText("carlos@example.com")))
            onView(withId(R.id.favoritesList)).check(matches(hasDescendant(withText("Rubén Blades"))))
            assertTrue(fixtures.requestedPaths.containsAll(listOf(
                "/collectors", "/collectors/100", "/collectors/100/albums",
            )))
            onView(allOf(withId(R.id.artistName), withText("Rubén Blades"))).perform(click())
            waitFor(allOf(withId(R.id.artistName), withText("Rubén Blades")))
            assertTrue(fixtures.requestedPaths.contains("/musicians/100"))
            pressBack()
        }
    }

    @Test fun hu05_emptyListShowsEmptyState() {
        fixtures.emptyCollectors = true
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.nav_collectors)).perform(click())
            waitForCollectorsPage(scenario)
            waitFor(allOf(withId(R.id.contentStateTitle), withText(R.string.collectors_empty)))
        }
    }

    @Test fun hu05_serverErrorCanRetry() {
        fixtures.responseCodes["/collectors"] = 500
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.nav_collectors)).perform(click())
            waitForCollectorsPage(scenario)
            waitFor(allOf(withId(R.id.contentStateTitle), withText(R.string.collectors_error)))
            fixtures.responseCodes.remove("/collectors")
            onView(allOf(withId(R.id.contentStateAction), isDescendantOfA(withId(R.id.collectorMessage))))
                .perform(click())
            waitFor(allOf(withId(R.id.collectorName), withText("Carlos Molina")))
        }
    }

    @Test fun hu06_emptyRelationsAndMissingCollectorHaveClearStates() {
        fixtures.collectorWithoutRelations = true
        ActivityScenario.launch<CollectorDetailActivity>(CollectorDetailActivity.intent(context, 100)).use {
            waitFor(allOf(withId(R.id.collectorName), withText("Carlos Molina")))
            onView(withId(R.id.favoritesEmpty)).check(matches(withText(R.string.collector_no_favorites)))
            onView(withId(R.id.collectionEmpty)).check(matches(withText(R.string.collector_no_albums)))
            onView(withId(R.id.commentsEmpty)).check(matches(withText(R.string.collector_no_comments)))
        }
        ActivityScenario.launch<CollectorDetailActivity>(CollectorDetailActivity.intent(context, 999)).use {
            waitFor(allOf(withId(R.id.contentStateTitle), withText(R.string.collector_not_found)))
        }
    }

    @Test fun hu06_errorCanRetryAndAlbumsOpenTheirDetail() {
        fixtures.responseCodes["/collectors/100"] = 500
        ActivityScenario.launch<CollectorDetailActivity>(CollectorDetailActivity.intent(context, 100)).use {
            waitFor(allOf(withId(R.id.contentStateTitle), withText(R.string.collector_detail_error)))
            fixtures.responseCodes.remove("/collectors/100")
            onView(allOf(withId(R.id.contentStateAction), isDescendantOfA(withId(R.id.detailMessage))))
                .perform(click())
            waitFor(allOf(withId(R.id.collectorName), withText("Carlos Molina")))
            onView(withId(R.id.collectionList)).perform(scrollTo())
            onView(withId(R.id.collectionList)).check(matches(hasDescendant(withText("Buscando América"))))
            onView(allOf(withId(R.id.albumTitle), withText("Buscando América"))).perform(click())
            waitFor(allOf(withId(R.id.detailName), withText("Buscando América")))
        }
    }

    @Test fun hu08_visitorCannotOpenTrackForm() {
        ActivityScenario.launch<AlbumDetailActivity>(AlbumDetailActivity.intent(context, 100)).use {
            waitFor(allOf(withId(R.id.detailName), withText("Buscando América")))
            onView(withId(R.id.addTrackFab)).check(matches(not(isDisplayed())))
        }
    }

    @Test fun hu08_validTrackUpdatesVisibleListAndPostPayload() {
        (roleRepository as FakeUserRoleRepository).let { kotlinx.coroutines.runBlocking { it.setRole(UserRole.COLLECTOR) } }
        ActivityScenario.launch<AlbumDetailActivity>(AlbumDetailActivity.intent(context, 100)).use {
            openTrackForm()
            onView(withId(R.id.nameInput)).perform(replaceText("  Pedro Navaja  "), closeSoftKeyboard())
            onView(withId(R.id.durationInput)).perform(replaceText(" 7:21 "), closeSoftKeyboard())
            onView(withId(R.id.saveButton)).perform(click())
            waitFor(allOf(withId(R.id.trackList), hasDescendant(withText("Pedro Navaja"))))
            onView(withId(R.id.detailContent)).perform(swipeUp())
            waitFor(allOf(withText("Pedro Navaja"), isCompletelyDisplayed()))
            val sent = JsonParser.parseString(fixtures.sentTrack).asJsonObject
            assertEquals("Pedro Navaja", sent["name"].asString)
            assertEquals("7:21", sent["duration"].asString)
            assertTrue(fixtures.requestedPaths.contains("POST /albums/100/tracks"))
        }
    }

    @Test fun hu08_invalidDurationAndEmptyNameNeverPost() {
        (roleRepository as FakeUserRoleRepository).let { kotlinx.coroutines.runBlocking { it.setRole(UserRole.COLLECTOR) } }
        ActivityScenario.launch<AlbumDetailActivity>(AlbumDetailActivity.intent(context, 100)).use {
            openTrackForm()
            onView(withId(R.id.durationInput)).perform(replaceText("7:99"), closeSoftKeyboard())
            onView(withId(R.id.saveButton)).perform(click())
            waitFor(allOf(withId(R.id.formMessage), withText(R.string.create_track_check_fields)))
            onView(withId(R.id.nameLayout)).check(matches(isDisplayed()))
            assertFalse(fixtures.requestedPaths.contains("POST /albums/100/tracks"))
        }
    }

    @Test fun hu08_serverFailureCanRetryWithoutLosingFields() {
        (roleRepository as FakeUserRoleRepository).let { kotlinx.coroutines.runBlocking { it.setRole(UserRole.COLLECTOR) } }
        fixtures.responseCodes["POST /albums/100/tracks"] = 500
        ActivityScenario.launch<AlbumDetailActivity>(AlbumDetailActivity.intent(context, 100)).use {
            openTrackForm()
            onView(withId(R.id.nameInput)).perform(replaceText("Pedro Navaja"), closeSoftKeyboard())
            onView(withId(R.id.durationInput)).perform(replaceText("7:21"), closeSoftKeyboard())
            onView(withId(R.id.saveButton)).perform(click())
            waitFor(allOf(withId(R.id.formMessage), withText(
                context.getString(R.string.create_track_failed, context.getString(R.string.request_server)),
            )))
            onView(withId(R.id.nameInput)).check(matches(withText("Pedro Navaja")))
            fixtures.responseCodes.remove("POST /albums/100/tracks")
            onView(withId(R.id.saveButton)).perform(click())
            waitFor(allOf(withId(R.id.trackList), hasDescendant(withText("Pedro Navaja"))))
        }
    }

    @Test fun hu08_cancelDoesNotPost() {
        (roleRepository as FakeUserRoleRepository).let { kotlinx.coroutines.runBlocking { it.setRole(UserRole.COLLECTOR) } }
        ActivityScenario.launch<AlbumDetailActivity>(AlbumDetailActivity.intent(context, 100)).use {
            openTrackForm()
            onView(withId(R.id.cancelButton)).perform(click())
            waitFor(withId(R.id.addTrackFab))
            assertFalse(fixtures.requestedPaths.contains("POST /albums/100/tracks"))
        }
    }

    private fun openTrackForm() {
        waitFor(allOf(withId(R.id.detailName), withText("Buscando América")))
        waitFor(withId(R.id.addTrackFab))
        onView(withId(R.id.addTrackFab)).perform(click())
        waitFor(withId(R.id.nameInput))
    }

    private fun waitForCollectorsPage(scenario: ActivityScenario<MainActivity>) {
        val deadline = System.currentTimeMillis() + 10_000L
        do {
            var idle = false
            scenario.onActivity { activity ->
                val pager = activity.findViewById<ViewPager2>(R.id.sectionPager)
                idle = pager.currentItem == 2 && pager.scrollState == ViewPager2.SCROLL_STATE_IDLE
            }
            if (idle) return
            Thread.sleep(50)
        } while (System.currentTimeMillis() < deadline)
        throw AssertionError("La pestaña Coleccionistas no terminó de abrir")
    }

    private fun waitFor(matcher: Matcher<View>) {
        val deadline = System.currentTimeMillis() + 10_000L
        var lastFailure: Throwable? = null
        do {
            try {
                onView(matcher).check(matches(isDisplayed()))
                return
            } catch (failure: AssertionError) {
                lastFailure = failure
            } catch (failure: NoMatchingViewException) {
                lastFailure = failure
            }
            Thread.sleep(100)
        } while (System.currentTimeMillis() < deadline)
        throw AssertionError(
            "La vista esperada no apareció antes de 10 s. Solicitudes: ${fixtures.requestedPaths}; respuestas: ${fixtures.responseCodes}",
            lastFailure,
        )
    }
}
