package com.ryoyakawai.felicareader.uitestutils

import android.app.Activity
import android.os.Build
import android.os.Environment

import androidx.test.espresso.Espresso
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.rules.TestWatcher
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random


class UiTestUtils {

    private lateinit var mActivity: Activity

    private var screenShotCounter = 0
    private lateinit var screenShotDir: String
    private val screenShotDirFormat = "yyyyMMdd_HHmmssSSS"
    private lateinit var filePrefix: String
    private var paramRemoveSuccessScreenShots = true

    private val msgTAG = "[MSG] <<📋🖌 UI TEST 🔎✔️>>"

    // ハッシュ値生成用文字列
    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    init {
        this.updateFilePrefix()
        Log.d(msgTAG, "[Begin Test] 👉 ${this.filePrefix}")
    }

    fun setActivity(mActivity: Activity) {
        this.mActivity = mActivity
    }

    fun prepareScreenShot(removeScreenShot: Boolean = true) {
        this.setParamRemoveSuccessScreenShots(removeScreenShot)

        val packageName = mActivity.packageName
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(this.screenShotDirFormat)
        val formatted = current.format(formatter)
        val sdcard = Environment.getExternalStorageDirectory()
        this.screenShotDir = "$sdcard/uitest/$formatted-$packageName-${randomString(10)}"
        File(this.screenShotDir).mkdirs()
        Log.d(msgTAG, "📷 saveDirectory=[${this.screenShotDir}]")
    }

    private fun updateFilePrefix() {
        this.filePrefix = randomString(10)
    }

    private fun setParamRemoveSuccessScreenShots(valToSet: Boolean) {
        this.paramRemoveSuccessScreenShots = valToSet
    }

    private fun captureScreenshot(path: File) {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).also {
            it.takeScreenshot(path)
        }
    }

    fun screenShot(type: String = "", message: String = "(NO MESSAGE)"): String {
        screenShotCounter += 1
        var picNumber = String.format("%06d", screenShotCounter)
        var path = "${this.screenShotDir}/Screenshot-${this.filePrefix}-$picNumber-$type.png"
        Log.d(msgTAG, "📷 🌉 filename=[$path]")
        var mPath = File(path)

        this.captureScreenshot(mPath)
        val sMessage = "📸 🖌 MESSAGE=[$message] PATH=[$mPath]"
        this.log_d(sMessage)
        while(!mPath.exists()) {
            this.sleep("SHR")
        }

        return mPath.toString()
    }

    fun removeSuccessScreenShots() {
        if(this.paramRemoveSuccessScreenShots == false) {
            return
        }
        File("${this.screenShotDir}/").walkTopDown().forEach {
            if(it.name.contains("${this.filePrefix}-.*.png$".toRegex())) {
                it.delete()
                while (it.exists()) {
                    this.sleep("SHR")
                }
            }
        }
        File("${this.screenShotDir}/").delete()
    }

    private fun findObjectPermission() {
         UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).also { uiDevice ->
             var allowPermissions = uiDevice.findObject(
                 UiSelector().clickable(true).checkable(false).index(1)
             )
             if (allowPermissions.exists()) {
                 try {
                     allowPermissions.click()
                 } catch (e: UiObjectNotFoundException) {
                     log_d("[Allow Button Does Not Found]")
                 }
             }
        }
    }

    fun allowPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23) {
            this.findObjectPermission()
        }
    }

    fun randomString(length: Int): String {
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    fun log_e(message: String) {
        Log.d(msgTAG, message)
    }

    fun log_d(message: String) {
        Log.d(msgTAG, message)
    }

    fun sleep(type: String) {
        var time = 1000
        when(type) {
            "LNG" -> time = 2500
            "NOR" -> time = 1000
            "SHR" -> time = 800
            "VSHR" -> time = 300
        }
        Thread.sleep(time.toLong())

        ViewActions.closeSoftKeyboard()
    }

    // childAtPosition()
    fun cAP(parentMatcher: Matcher<View>, position: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    // get and return text
    fun getText(matcher: Matcher<View>): String {
        var stringHolder = emptyArray<String>()
        Espresso.onView(matcher).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(TextView::class.java)
            }

            override fun getDescription(): String {
                return "getting text from a TextView"
            }

            override fun perform(uiController: UiController, view: View) {
                val tv = view as TextView //Save, because of check in getConstraints()
                stringHolder = arrayOf(tv.text.toString())
            }
        })
        return stringHolder[0]
    }

}

class ScreenshotTakingRule(mUTs: UiTestUtils) : TestWatcher() {
    private var mUTs = mUTs

    override fun failed(e: Throwable?, description: org.junit.runner.Description?) {
        super.failed(e, description)
        val path = mUTs.screenShot("FAIL-$description")
        mUTs.log_d(">>> !!! TEST FAILED !!! <<< ScreenShot Taken method=[$description] filename=[$path]")
    }
}
