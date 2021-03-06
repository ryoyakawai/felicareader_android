package com.ryoyakawai.felicareader


import com.ryoyakawai.felicareader.api.response.SinglePostResponse

interface MainActivityViewContract {

    fun updateMainContentText(text: String)

    fun toggleNfcReaderState(mode: Boolean)

    fun updateNfcIdm(text: String?)

    fun handleOkButton()

    fun handleSuccess(result: Array<SinglePostResponse>)

    fun handleError(message: String)
}
