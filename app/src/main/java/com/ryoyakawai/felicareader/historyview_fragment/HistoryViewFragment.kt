package com.ryoyakawai.felicareader.historyview_fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ryoyakawai.felicareader.R
import com.ryoyakawai.felicareader.felicalibs.FelicaLibsReader


class HistoryViewFragment : Fragment() {

    private val tTAG = "ryoyakawai_falicareader"
    private lateinit var mView : View

    var mAllHistoryList: ArrayList<RecyclerviewAdapter.RawResponse> = arrayListOf<RecyclerviewAdapter.RawResponse>()

    private val felicaclibsreader = FelicaLibsReader()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.mView = inflater.inflate(R.layout.fragment_history_view, container, false)

        val bundle = arguments
        val arrayAllHistory: ArrayList<ByteArray>  = bundle!!.getSerializable("arrayAllHistory") as ArrayList<ByteArray>

        // consoleに出力

        (0 until arrayAllHistory.size).forEach { i ->
            mAllHistoryList.add(RecyclerviewAdapter.RawResponse(
                i, arrayAllHistory[i], R.drawable.sample00))

            val oneRecordString = felicaclibsreader.bytesToHexString(arrayAllHistory[i])
            Log.d(tTAG, "#$i OneRecord=[$oneRecordString]")
        }

        // RecyclerViewの取得
        val recyclerView = mView.findViewById<RecyclerView>(R.id.history_item_recycler_view) as RecyclerView

        // LayoutManagerの設定
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        // CustomAdapterの生成と設定
        var mAdapter = RecyclerviewAdapter(mAllHistoryList)
        recyclerView.adapter = mAdapter

        return this.mView
    }

}
