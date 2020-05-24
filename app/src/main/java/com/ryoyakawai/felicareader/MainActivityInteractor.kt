package com.ryoyakawai.felicareader

import com.ryoyakawai.felicareader.api.APIClient
import com.ryoyakawai.felicareader.api.ApiConnection
import com.ryoyakawai.felicareader.api.response.SinglePostResponse
import io.reactivex.Single
import org.json.JSONObject

class MainActivityInteractor {

    fun simpleSampleResponse() : JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("userId", "1")
        jsonObject.put("id", "2")
        jsonObject.put("success", "true")
        return jsonObject
    }

    fun getPostsById(postId: Int) : Single<Array<SinglePostResponse>> {
        val mApiConnection: ApiConnection = APIClient.mApiConnection!!
        return mApiConnection.commentByPostId(postId)
    }
}
