package com.example.vinilosmobile

import android.content.Intent
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vinilosmobile.di.DatabaseModule
import com.example.vinilosmobile.di.NetworkModule
import com.example.vinilosmobile.di.RepositoryModule
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import com.example.vinilosmobile.feature.albumdetail.AlbumDetailActivity
import com.example.vinilosmobile.feature.artistdetail.ArtistDetailActivity
import com.example.vinilosmobile.feature.home.MainActivity
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, NetworkModule::class, DatabaseModule::class)
class Sprint1EspressoTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @BindValue @JvmField val fixtures = FixtureApiInterceptor()
    @BindValue @JvmField val roleRepository: UserRoleRepository = FakeUserRoleRepository()

    @Before fun prepare() {
        hiltRule.inject()
    }

    @Test fun hu01_catalogLoadsThroughRetrofitAndRepository() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForVisible(allOf(withId(R.id.albumTitle), withText("Buscando América")))
            onView(withId(R.id.albumGrid)).check(matches(hasDescendant(withText("A Night at the Opera"))))
            assertTrue(fixtures.requestedPaths.contains("/albums"))
        }
    }

    @Test fun hu01_emptyCatalogShowsEmptyState() {
        fixtures.emptyAlbums = true
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForVisible(allOf(withId(R.id.contentStateTitle), withText(R.string.catalog_empty)))
        }
    }

    @Test fun hu01_serverErrorCanRetry() {
        fixtures.responseCodes["/albums"] = 500
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForVisible(allOf(withId(R.id.contentStateTitle), withText(R.string.catalog_error_title)))
            fixtures.responseCodes.remove("/albums")
            onView(allOf(withId(R.id.contentStateAction), isDescendantOfA(withId(R.id.catalogMessage))))
                .perform(click())
            waitForVisible(allOf(withId(R.id.albumTitle), withText("Buscando América")))
        }
    }

    @Test fun hu01_networkFailureShowsRoomCache() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForVisible(allOf(withId(R.id.albumTitle), withText("Buscando América")))
        }
        fixtures.responseCodes["/albums"] = 500
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForVisible(withId(R.id.cachedNotice))
            onView(withId(R.id.albumGrid)).check(matches(hasDescendant(withText("Buscando América"))))
        }
    }

    @Test fun hu02_cardOpensAlbumActivityWithTracksAndComments() {
        (roleRepository as FakeUserRoleRepository).run { kotlinx.coroutines.runBlocking { setRole(UserRole.COLLECTOR) } }
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForVisible(allOf(withId(R.id.albumTitle), withText("Buscando América")))
            onView(allOf(withId(R.id.albumTitle), withText("Buscando América"))).perform(click())
            waitForVisible(allOf(withId(R.id.detailName), withText("Buscando América")))
            onView(withId(R.id.trackList)).check(matches(hasDescendant(withText("Decisiones"))))
            onView(withId(R.id.commentList)).check(matches(hasDescendant(withText("Gran álbum"))))
            onView(withId(R.id.addTrackFab)).check(matches(isDisplayed()))
            assertTrue(fixtures.requestedPaths.contains("/albums/100"))
            pressBack()
        }
    }

    @Test fun hu02_missingAlbumShowsNotFoundAndVisitorHasNoAction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<AlbumDetailActivity>(AlbumDetailActivity.intent(context, 999L)).use {
            waitForVisible(allOf(withId(R.id.contentStateTitle), withText(R.string.album_not_found)))
            onView(withId(R.id.addTrackFab)).check(matches(not(isDisplayed())))
        }
    }

    @Test fun hu02_albumWithoutTracksShowsEmptyMessage() {
        fixtures.albumWithoutTracks = true
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<AlbumDetailActivity>(AlbumDetailActivity.intent(context, 100L)).use {
            waitForVisible(allOf(withId(R.id.detailName), withText("Buscando América")))
            onView(withId(R.id.tracksEmpty)).check(matches(withText(R.string.album_no_tracks)))
        }
    }

    @Test fun hu02_serverErrorCanRetry() {
        fixtures.responseCodes["/albums/100"] = 500
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<AlbumDetailActivity>(AlbumDetailActivity.intent(context, 100L)).use {
            waitForVisible(allOf(withId(R.id.contentStateTitle), withText(R.string.album_detail_error)))
            fixtures.responseCodes.remove("/albums/100")
            onView(allOf(withId(R.id.contentStateAction), isDescendantOfA(withId(R.id.detailMessage))))
                .perform(click())
            waitForVisible(allOf(withId(R.id.detailName), withText("Buscando América")))
        }
    }

    @Test fun hu03_artistListFiltersMusiciansAndBands() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.nav_artists)).perform(click())
            waitForVisible(allOf(withId(R.id.artistName), withText("Queen")))
            onView(withId(R.id.filterBands)).perform(click())
            onView(withId(R.id.artistList)).check(matches(hasDescendant(withText("Queen"))))
            onView(withId(R.id.artistList)).check(matches(not(hasDescendant(withText("Rubén Blades")))))
            onView(withId(R.id.filterMusicians)).perform(click())
            onView(withId(R.id.artistList)).check(matches(hasDescendant(withText("Rubén Blades"))))
            assertTrue(fixtures.requestedPaths.containsAll(listOf("/musicians", "/bands")))
        }
    }

    @Test fun hu03_emptyArtistsShowEmptyState() {
        fixtures.emptyArtists = true
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.nav_artists)).perform(click())
            waitForVisible(allOf(withId(R.id.contentStateTitle), withText(R.string.artists_empty)))
        }
    }

    @Test fun hu03_serverErrorCanRetry() {
        fixtures.responseCodes["/bands"] = 500
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.nav_artists)).perform(click())
            waitForVisible(allOf(withId(R.id.contentStateTitle), withText(R.string.artists_error)))
            fixtures.responseCodes.remove("/bands")
            onView(allOf(withId(R.id.contentStateAction), isDescendantOfA(withId(R.id.artistMessage))))
                .perform(click())
            waitForVisible(allOf(withId(R.id.artistName), withText("Queen")))
        }
    }

    @Test fun hu04_musicianDetailDisplaysAlbumsAndPrize() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.nav_artists)).perform(click())
            waitForVisible(allOf(withId(R.id.artistName), withText("Rubén Blades")))
            onView(allOf(withId(R.id.artistName), withText("Rubén Blades"))).perform(click())
            waitForVisible(withId(R.id.artistDateLabel))
            onView(withId(R.id.artistAlbumList)).check(matches(hasDescendant(withText("Buscando América"))))
            onView(withId(R.id.awardList)).check(matches(hasDescendant(withText("Grammy Award"))))
            assertTrue(fixtures.requestedPaths.containsAll(listOf("/musicians/100", "/performerprizes")))
            pressBack()
        }
    }

    @Test fun hu04_bandDetailDisplaysMembersAndCreationDate() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent: Intent = ArtistDetailActivity.intent(context, ArtistKey(101L, ArtistType.BAND))
        ActivityScenario.launch<ArtistDetailActivity>(intent).use {
            waitForVisible(allOf(withId(R.id.artistName), withText("Queen")))
            onView(withId(R.id.memberList)).check(matches(hasDescendant(withText("Freddie Mercury"))))
            onView(withId(R.id.artistDateLabel)).check(matches(withText(R.string.artist_creation_date)))
            assertTrue(fixtures.requestedPaths.contains("/bands/101"))
        }
    }

    @Test fun hu04_missingArtistShowsNotFound() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = ArtistDetailActivity.intent(context, ArtistKey(999L, ArtistType.MUSICIAN))
        ActivityScenario.launch<ArtistDetailActivity>(intent).use {
            waitForVisible(allOf(withId(R.id.contentStateTitle), withText(R.string.artist_not_found)))
        }
    }

    @Test fun hu04_serverErrorCanRetry() {
        fixtures.responseCodes["/musicians/100"] = 500
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = ArtistDetailActivity.intent(context, ArtistKey(100L, ArtistType.MUSICIAN))
        ActivityScenario.launch<ArtistDetailActivity>(intent).use {
            waitForVisible(allOf(withId(R.id.contentStateTitle), withText(R.string.artist_detail_error)))
            fixtures.responseCodes.remove("/musicians/100")
            onView(allOf(withId(R.id.contentStateAction), isDescendantOfA(withId(R.id.detailMessage))))
                .perform(click())
            waitForVisible(allOf(withId(R.id.artistName), withText("Rubén Blades")))
        }
    }

    private fun waitForVisible(matcher: Matcher<View>) {
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
        throw AssertionError("La vista esperada no apareció antes de 10 s", lastFailure)
    }
}
