package com.ryoyakawai.felicareader.api

import com.ryoyakawai.felicareader.api.response.SinglePostResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


interface ApiConnection {
    @Headers(
        "Accept: application/json",
        "Content-type: application/json"
    )
    @GET("comments")
    fun commentByPostId(@Query("postId") postId: Int): Single<Array<SinglePostResponse>>

}
