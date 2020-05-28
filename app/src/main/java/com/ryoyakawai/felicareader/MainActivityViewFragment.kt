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
import java.util.*

class MainActivityViewFragment : Fragment(), MainActivityViewContract,  NfcAdapter.ReaderCallback {

    private var idmText_default: String = "--"
    private val tTAG = "ryoyakawai_falicareader"
    private lateinit var mView : View

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var nfcReaderOn: Button
    private lateinit var nfcReaderOff: Button
    private lateinit var nfcIdmText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tTAG, "fragment: 🙆‍♀️ 🙆‍♂️‍")

        this.mView = inflater.inflate(R.layout.content_main, container, false)
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

        //get idm
        val idm: ByteArray = tag.id
        val idmString: String? = this.bytesToHexString(idm)
        //val idmString: String = idmString.toString().toUpperCase(Locale.getDefault())

        //idm取るだけじゃなくてread,writeしたい場合はtag利用してごにょごにょする
        activity?.runOnUiThread {
            this.updateNfcIdm(idmString)
        }
    }

    private fun bytesToHexString(bytes: ByteArray): String? {
        val sb = java.lang.StringBuilder()
        val formatter = Formatter(sb)
        for (b in bytes) {
            formatter.format("%02x", b)
        }
        return sb.toString().toUpperCase(Locale.getDefault())
    }

    override fun updateNfcIdm(_text: String?) {
        var text: String? = _text
        if(text == null) text = this.idmText_default
        this.nfcIdmText.text = text
        Log.d(tTAG, "idm=[$text]")
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