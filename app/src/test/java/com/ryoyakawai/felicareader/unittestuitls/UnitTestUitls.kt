package com.ryoyakawai.felicareader.unittestuitls

import com.ryoyakawai.felicareader.api.APIClient
import com.ryoyakawai.felicareader.api.ApiConnection
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.mockito.Mockito

// [Unit testing support - Android Studio Project Site]
// http://tools.android.com/tech-docs/unit-testing-support#TOC-Method-...-not-mocked.-

class UnitTestUtils {

    private val mMockServer: MockWebServer = MockWebServer()
    var mockServerBehaviorSwitcher: (() -> Dispatcher)? = null

    fun startMockServer(port: Int = 8080) {
        mMockServer.start(port)
    }

    fun shutdownMockServer() {
        mMockServer.shutdown()
    }

    fun setupMockServer(path: String = "/"): ApiConnection {
        val client = OkHttpClient()

        val dispatchResponse: Dispatcher? = this.mockServerBehaviorSwitcher?.invoke()
        mMockServer.setDispatcher(dispatchResponse)

        var url = mMockServer.url(path).toString()
        var mAPIClient = Mockito.spy(APIClient)
        var retrofit = mAPIClient.buildRetrofit2Client(client, url)

        return retrofit.create(ApiConnection::class.java)
    }

    fun assertDataClass(expected: Any, actual: Any, debug:Boolean = false) {
        val javaClass = expected::class.java
        javaClass.declaredFields.forEach { field ->
            field.isAccessible = true
            if(debug) {
                System.out.println("[" + field.name + "]")
                System.out.println("    >>>>   actual=[" + field.get(actual) + "]")
                System.out.println("    >>>> expected=[" + field.get(expected) + "]")
            }
            Assert.assertEquals(field.get(expected), field.get(actual))
        }
    }

    fun prepareRxForTesting() {
        this.resetRx()
        this.switchToTrampolineScheduler()
    }

    private fun resetRx() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    private fun switchToTrampolineScheduler() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }



}
