package com.ryoyakawai.felicareader

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

import com.google.android.material.snackbar.Snackbar
import com.ryoyakawai.felicareader.api.response.SinglePostResponse
import com.ryoyakawai.felicareader.felicalibs.BasicTagInfo
import com.ryoyakawai.felicareader.felicalibs.FelicaLibsReader
import com.ryoyakawai.felicareader.historyview_fragment.HistoryViewFragment

class MainActivityViewFragment : Fragment(), MainActivityViewContract,  NfcAdapter.ReaderCallback {

    private var idmText_default: String = "--"
    private val tTAG = "ryoyakawai_falicareader"
    private lateinit var mView : View

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var nfcReaderOn: Button
    private lateinit var nfcReaderOff: Button
    private lateinit var nfcIdmText: TextView

    private val felicaclibsreader = FelicaLibsReader()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.mView = inflater.inflate(R.layout.activity_main_view, container, false)
        this.nfcReaderOn = this.mView.findViewById(R.id.main_content_nfc_reader_on)
        this.nfcReaderOff = this.mView.findViewById(R.id.main_content_nfc_reader_off)
        this.nfcIdmText = mView.findViewById(R.id.main_content_nfc_idm)

        updateMainContentText("Hello World!!")

        // vvv nfc vvv
        // toggle button
        toggleNfcReaderState(false)

        var nfcIdmText: TextView = mView.findViewById(R.id.main_content_nfc_idm)
        nfcIdmText.text = idmText_default

        nfcAdapter = NfcAdapter.getDefaultAdapter(this.context)

        val mMainActivity = activity as MainActivity?
        this.nfcReaderOn.setOnClickListener { view ->
            toggleNfcReaderState(true)
            // Trun NFC Reder On
            this.nfcAdapter.enableReaderMode(
                mMainActivity,
                this,
                NfcAdapter.FLAG_READER_NFC_F,
                null
            )
        }

        this.nfcReaderOff.setOnClickListener { view ->
            toggleNfcReaderState(false)
            nfcAdapter.disableReaderMode(this.context as Activity?);
        }

        return mView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_reset_idm_text -> {
                updateNfcIdm(idmText_default)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    // mode = true (state of reader is ON)
    //      = false (state of reader is OFF)
    override fun toggleNfcReaderState(mode: Boolean) {
        val nfcReaderOn: Button = mView.findViewById(R.id.main_content_nfc_reader_on)
        val nfcReaderOff: Button = mView.findViewById(R.id.main_content_nfc_reader_off)
        val nfcIdmText: TextView = mView.findViewById(R.id.main_content_nfc_idm)

        when(mode) {
            true -> {
                nfcIdmText.text = idmText_default
                nfcReaderOn.isEnabled = false
                nfcReaderOff.isEnabled = true
            }
            false -> {
                nfcReaderOn.isEnabled = true
                nfcReaderOff.isEnabled = false
            }
        }
    }

    override fun onTagDiscovered(tag: Tag) {
        Log.d(tTAG, "Tag discoverd.")

        val tagInfo: BasicTagInfo = felicaclibsreader.getBasicInformation(tag)
        val idmString = felicaclibsreader.bytesToHexString(tagInfo.idm)
        Log.d(tTAG, "idm=[$idmString]")

        val midString = felicaclibsreader.bytesToHexString(tagInfo.mid)
        Log.d(tTAG, "mid=[$midString]")

        val pmmString = felicaclibsreader.bytesToHexString(tagInfo.pmm)
        Log.d(tTAG, "pmm=[$pmmString]")

        val systemCodeString = felicaclibsreader.bytesToHexString(tagInfo.systemCode)
        Log.d(tTAG, "systemCode=[$systemCodeString]")

        // service code suica 固定
        val serviceCode = byteArrayOf(0x09.toByte(), 0x0f.toByte())
        val arrayAllHistory = felicaclibsreader.getAllTransactionHistory(tagInfo.idm, serviceCode)
        val responseString = felicaclibsreader.bytesToHexString(arrayAllHistory[0])
        Log.d(tTAG, "polling_response=[$responseString]")

        // Fragment を移動
        val mHistoryViewFragment = HistoryViewFragment()
        val fragmentManager = fragmentManager
        if (fragmentManager != null) {
            val bundle = Bundle()
            bundle.putSerializable("arrayAllHistory", arrayAllHistory)
            mHistoryViewFragment.arguments = bundle;

            val transaction = fragmentManager?.beginTransaction()
            transaction.replace(R.id.fragment_container, mHistoryViewFragment);
            transaction.addToBackStack(null)
            transaction.commit()
        }

        activity?.runOnUiThread {
            this.updateNfcIdm(idmString)
        }
    }

    override fun updateNfcIdm(_text: String?) {
        var text: String? = _text
        if(text == null) text = this.idmText_default
        this.nfcIdmText.text = text
    }

    override fun updateMainContentText(text: String) {
        val messageView: TextView = mView.findViewById(R.id.main_content_text)
        messageView.text = text
    }

    override fun handleOkButton() {
        Snackbar.make(mView, "Permission Sample", Snackbar.LENGTH_SHORT)
            .setAction("Action", null).show()
    }

    override fun handleSuccess(result: Array<SinglePostResponse>) {
        Log.d(tTAG, "SUCCESS")
    }

    override fun handleError(message: String) {
        Log.e(tTAG, "ERROR")
    }
}
