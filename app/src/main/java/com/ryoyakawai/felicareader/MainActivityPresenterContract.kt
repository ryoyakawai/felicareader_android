package com.ryoyakawai.felicareader

import org.json.JSONObject

interface MainActivityPresenterContract {

    fun setView(view: MainActivityViewContract)

    fun getSimpleJsonSampleResponse(): JSONObject

    fun getJsonSampleResponse()//: Single<CommentsPostId1>

}
