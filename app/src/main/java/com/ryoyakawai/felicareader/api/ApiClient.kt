package com.ryoyakawai.felicareader.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate

import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object APIClient: ApiClientContract {

    // [Retrofit+RxJava+kotlinを使ったAPIコール処理を作ってみた - Qiita]
    // https://qiita.com/motomiya326/items/f59f0ddf400da4050fe8

    // [JSONPlaceholder - Fake online REST API for developers]
    // https://jsonplaceholder.typicode.com/

    // [RxJava2使おうとしたら「More than one file was found with OS independent path ‘META-INF/rxjava.properties’」が出る時の対処法 – エンジニアの便利手帳]
    //  http://3jigen.net/2018/10/post-849/

    // [Android 9(Pie)でHTTP通信を有効にする - Qiita]
    // https://qiita.com/b_a_a_d_o/items/afa0d83bbffdb5d4f6be

    // [Android でインターネットに接続するためのパーミッションを設定する - Qiita]
    // https://qiita.com/karur4n/items/5b439850caa4ae5b05d9


    private const val API_ENDPOINT_BASE_URL = "https://jsonplaceholder.typicode.com/"
    private const val X_HEADER_NAME = "X-UITEST-SAMPLE"
    private const val X_HEADER_VERSION = "1.0.0"
    private const val CONNECTION_TIMEOUT: Long = 50
    private const val CONNECTION_READ_TIMEOUT: Long = 50
    private const val CONNECTION_WRITE_TIMEOUT: Long = 50

    var mApiConnection: ApiConnection? = null

    init {
        val okHttpClient =
            buildOkHttp3Client()
        val retrofit =
            buildRetrofit2Client(
                okHttpClient,
                API_ENDPOINT_BASE_URL
            )

        setConnection(
            retrofit.create(
                ApiConnection::class.java
            )
        )
    }

    override fun setConnection(apiConnection: ApiConnection) {
        mApiConnection = apiConnection
    }

    private fun setSslOkHttp3Client(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val x509TrustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }

        val trustManagers = arrayOf<TrustManager>(x509TrustManager)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagers, null)

        return buildOkHttp3Client()
    }

    //override fun buildOkHttp3Client(): OkHttpClient {
    private fun buildOkHttp3Client(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        //logging.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(CONNECTION_READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(CONNECTION_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor {
                it.proceed(it.request()
                    .newBuilder()
                    .addHeader(
                        X_HEADER_NAME,
                        X_HEADER_VERSION
                    )
                    .build())
            }
            .addInterceptor(logging)
            .build()
    }

    override fun buildRetrofit2Client(okHttpClient: OkHttpClient, apiBaseUrl: String): Retrofit {
    //private fun buildRetrofit2Client(okHttpClient: OkHttpClient, apiBaseUrl: String): Retrofit {
        val jsonHandler: Gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(apiBaseUrl)
            .client(setSslOkHttp3Client())
            .addConverterFactory(GsonConverterFactory.create(jsonHandler))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

}
