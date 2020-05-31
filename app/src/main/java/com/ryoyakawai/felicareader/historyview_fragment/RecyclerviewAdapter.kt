package com.ryoyakawai.felicareader.historyview_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ryoyakawai.felicareader.R
import com.ryoyakawai.felicareader.felicalibs.FelicaLibsReader

//class RecyclerviewAdapter(private val animalList: ArrayList<Animal>): RecyclerView.Adapter<RecyclerviewAdapter.ViewHolder>() {
class RecyclerviewAdapter(private val historyList: ArrayList<RawResponse>): RecyclerView.Adapter<RecyclerviewAdapter.ViewHolder>() {

    private val felicaclibsreader = FelicaLibsReader()

    data class RawResponse(
        val listIndex:  Int,
        val rawData: ByteArray,
        val imageId: Int
    )

    // Viewの初期化
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView
        val raw_data: TextView
        val list_index: TextView

        init {
            image = view.findViewById(R.id.image)
            raw_data = view.findViewById(R.id.raw_data)
            list_index = view.findViewById(R.id.list_index)
        }
    }

    // レイアウトの設定
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.fragment_history_view_list_item_recycler_view, viewGroup, false)
        return ViewHolder(view)
    }

    // Viewの設定
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val historyList = historyList[position]

        viewHolder.image.setImageResource(historyList.imageId)
        viewHolder.raw_data.text = felicaclibsreader.bytesToHexString(historyList.rawData)
        viewHolder.list_index.text = "#" + historyList.listIndex.toString()
    }

    // 表示数を返す
    override fun getItemCount() = historyList.size
}
