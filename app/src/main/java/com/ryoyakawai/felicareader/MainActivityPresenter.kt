package com.ryoyakawai.felicareader

import org.json.JSONObject
import android.util.Log
import com.ryoyakawai.felicareader.api.response.SinglePostResponse
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.lang.NullPointerException


class MainActivityPresenter :
    MainActivityPresenterContract {

    private var mView: MainActivityViewContract? = null
    private var mModel: MainActivityInteractor? = null
    private val mDisposable = CompositeDisposable()
    private val tTAG = "ryoyakawai_falicareader"

    init {
        mModel = MainActivityInteractor()
    }

    override fun setView(view: MainActivityViewContract) {
        mView = view
    }

    override fun getJsonSampleResponse() {
        try {
            val disposable: Disposable = mModel!!.getPostsById(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ ResCommentsPostId: Array<SinglePostResponse> ->
                    mView?.let {
                        for (item in ResCommentsPostId) {
                            val javaClass = item::class.java
                            javaClass.declaredFields.forEach { field ->
                                field.isAccessible = true
                                Log.d(tTAG, field.name + ":" + field.get(item))
                            }
                        }
                        it.handleSuccess(ResCommentsPostId)
                    }
                }, { error: Throwable ->
                    error.let {
                        mView?.handleError(it.message.toString())
                    }
                })
            mDisposable.add(disposable)
        } catch(e: NullPointerException) {
            mView?.handleError(e.toString())
        }
    }

    override fun getSimpleJsonSampleResponse(): JSONObject {
        return mModel!!.simpleSampleResponse()
    }

}
