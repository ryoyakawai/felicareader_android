package com.ryoyakawai.felicareader

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*


import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import com.ryoyakawai.felicareader.R

import com.ryoyakawai.felicareader.uitestutils.ScreenshotTakingRule
import com.ryoyakawai.felicareader.uitestutils.UiTestUtils
import junit.framework.TestCase.assertEquals
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
@SdkSuppress(minSdkVersion = 26)
@LargeTest
class MainActivityInstrumentedTest {

    private val _packageName = "com.ryoyakawai.felicareader"
    private val mUTs: UiTestUtils =
        UiTestUtils()

    @Rule
    @JvmField
    val cGrantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Rule
    @JvmField
    val mActivityTestRule: ActivityTestRule<MainActivity> =
        ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    val screenshotRule =
        ScreenshotTakingRule(this.mUTs)

    @Before
    fun setup() {
        this.mUTs.setActivity(mActivityTestRule.activity)
    }

    @After
    fun teardown() { }

    @Test
    fun useAppContext() {
        this.mUTs.prepareScreenShot()

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(_packageName, appContext.packageName)
        this.mUTs.sleep("SHR")

        this.mUTs.removeSuccessScreenShots()
    }

    @Test
    fun checkTextHelloWorld() {
        this.mUTs.prepareScreenShot()

        onView(withId(R.id.main_content_text)).check(matches(withText(containsString("Hello World!"))))
        mUTs.log_d("useAppContext()")
        this.mUTs.sleep("SHR")

        this.mUTs.removeSuccessScreenShots()
    }

    @Test
    fun checkButtonIncrementFloating() {
        this.mUTs.prepareScreenShot(false)
        //
        // To check initial counter
        mUTs.screenShot("", "Hello World!!")
        var actualCount = this.mUTs.getText(withId(R.id.main_content_text))
        this.mUTs.log_d("[Counter initial] 🍏 expected=[Hello World!!] actual=[$actualCount]")
        assertEquals("[Counter initial] 🍏", "Hello World!!", actualCount)

        // click home button to fail test
        //onView(withContentDescription(android.R.id.home)).perform(click())

        //
        // To check whether increment button works properly
        val willTap = 5
        val incrementButton =  withId(R.id.increment_fab_text)

        for(i in 1..willTap) {
            // Tap increment button
            mUTs.screenShot("", "BEFORE >>> カウンター：インクリメント IDX=[$i]")
            onView(incrementButton).perform(click())

            mUTs.allowPermissionsIfNeeded()

            mUTs.screenShot("", "AFTER >>> カウンター：インクリメント IDX=[$i]")
            actualCount = this.mUTs.getText(withId(R.id.main_content_text))
            this.mUTs.log_d("[Counter SEQ] 🍏🍎 expected=[$i] actual=[$actualCount]")
            assertEquals("[Counter SEQ] 🍏🍎", i.toString(), actualCount)

            // Wait for snack bar disappears
            val snackBarTapped = allOf(withId(R.id.snackbar_text), withText("Tapped $i times."))
            waitForSnackbarDisappear(snackBarTapped)
            this.mUTs.sleep("SHR")
        }
        this.mUTs.sleep("SHR")

        //
        // To check whether reset counter button works properly
        mUTs.screenShot("", "[BEFORE] カウンターのリセット")
        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        this.mUTs.sleep("SHR")
        val menuButton = allOf(
                withId(R.id.title), withText("Reset Counter"),
                this.mUTs.cAP(this.mUTs.cAP(withId(R.id.content), 0),0),
                isDisplayed())
        onView(menuButton).perform(click())
        this.mUTs.sleep("SHR")
        mUTs.screenShot("", "[AFTER] カウンターのリセット")
        actualCount = this.mUTs.getText(withId(R.id.main_content_text))
        this.mUTs.log_d("[Counter Clear] 🍏🍎🍐 expected=[0] actual=[$actualCount]")
        assertEquals("[Counter Clear] 🍏🍎🍐", actualCount, "0")

        this.mUTs.removeSuccessScreenShots()
    }

    private fun waitForSnackbarDisappear(targetMatcher: Matcher<View>) {
        var doLoop = true
        while(doLoop) {
            try {
                onView(targetMatcher).check(matches(isDisplayed()))
                this.mUTs.sleep("SHR")
            } catch (e: NoMatchingViewException) {

                this.mUTs.log_e("This error is telling test runner that there are no snackbar on screen.")
                doLoop = false
            }
        }
    }

}

