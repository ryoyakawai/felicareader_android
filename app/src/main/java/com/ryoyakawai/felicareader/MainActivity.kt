package com.ryoyakawai.felicareader

import android.Manifest
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.ryoyakawai.felicareader.api.response.SinglePostResponse
import java.util.*


class MainActivity : AppCompatActivity(), MainActivityViewContract, NfcAdapter.ReaderCallback {

    private var mPresenter: MainActivityPresenterContract? = null
    private var counter: Int = 0
    private val tTAG = "ryoyakawai_falicareader"
    private val reqCodeWriteExternalStorage = 123
    private var permissionGrantedBehavior: ((activity: MainActivity) -> Unit) = {}
    private var permissionDeniedBehavior: ((activity: MainActivity) -> Unit ) = {}
    private var permissionUndefinedBehavior: ((activity: MainActivity) -> Unit ) = {}

    // vvv nfc vvv
    private lateinit var nfcIdmText: TextView
    private lateinit var nfcReaderOn: Button
    private lateinit var nfcReaderOff: Button
    private lateinit var nfcAdapter: NfcAdapter
    // ^^^ nfc ^^^

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.mPresenter = MainActivityPresenter()
        this.mPresenter!!.setView(this)

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar)

        updateMainContentText("Hello World!!")

        // vvv nfc vvv

        var nfcIdmText: TextView = findViewById(R.id.main_content_nfc_idm)
        val nfcReaderOn: Button = findViewById(R.id.main_content_nfc_reader_on)
        val nfcReaderOff: Button = findViewById(R.id.main_content_nfc_reader_off)

        //nfcAdapter初期化
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // toggle button
        toggleNfcReaderState(false)

        nfcIdmText.text = "--"

        // event listener for making ReaderOn
        nfcReaderOn.setOnClickListener { view ->
            toggleNfcReaderState(true)
            // Trun NFC Reder On
            nfcAdapter.enableReaderMode(
                this@MainActivity,
                this@MainActivity,
                NfcAdapter.FLAG_READER_NFC_F,
                null
            )
        }

        nfcReaderOff.setOnClickListener { view ->
            toggleNfcReaderState(false)

            nfcAdapter.disableReaderMode(this);

        }
        // ^^^ nfc ^^^


        val emailFab: FloatingActionButton = findViewById(R.id.email_fab)
        emailFab.setOnClickListener { view ->

            this.requestWriteExtStoragePermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE, reqCodeWriteExternalStorage)

            permissionGrantedBehavior = {
                Log.d(tTAG, "LAMBDA: 🙆‍♀️ PERMISSION GRANTED 🙆‍♂️‍")
                Toast.makeText(it, "🙆‍♀️ Permission Granted 🙆‍♂️" ,Toast.LENGTH_SHORT).show()

                val json = this.mPresenter!!.getSimpleJsonSampleResponse()
                Log.d("JSON_OUT", json.getString("userId") )
                Log.d("JSON_OUT", json.getString("id") )
                Log.d("JSON_OUT", json.getString("success") )

                mPresenter!!.getJsonSampleResponse()

            }

            permissionDeniedBehavior = {
                Toast.makeText(it, "🙅 Permission Denied 🙅‍" ,Toast.LENGTH_SHORT).show()
                Log.d(tTAG, "LAMBDA: 🙅 ‍PERMISSION Denied 🙅‍")
            }

            permissionUndefinedBehavior = {
                Toast.makeText(it, "🤷 Something Went Wrong 🤷‍‍" ,Toast.LENGTH_SHORT).show()

            }

            this.counter += 1
            updateMainContentText(this.counter.toString())

            handleOkButton(view)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        createFloatMenu(R.menu.menu_main, menu)
        //menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_reset_counter -> {
                restCounter()
                updateMainContentText(this.counter.toString())
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun createFloatMenu(id: Int, menu: Menu) {
        menuInflater.inflate(R.menu.menu_main, menu)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            reqCodeWriteExternalStorage -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(tTAG, "GRANTED")
                    permissionGrantedBehavior.invoke(this)
                } else {
                    Log.d(tTAG, "DENIED")
                    permissionDeniedBehavior.invoke(this)
                }
            }
            else -> {
                Log.e(tTAG, "[UNDEFINED] requestCode")
                permissionUndefinedBehavior.invoke(this)
            }
        }
    }

    // vvv nfc vvv
    override fun onTagDiscovered(tag: Tag) {
        Log.d(tTAG, "Tag discoverd.")

        //get idm
        val idm: ByteArray = tag.getId()

        // bytesToHexString()
        val sb = StringBuilder()
        val formatter = Formatter(sb)
        for (b in idm) {
            formatter.format("%02x", b)
        }
        val idmString: String = sb.toString().toUpperCase(Locale.getDefault())

        //idm取るだけじゃなくてread,writeしたい場合はtag利用してごにょごにょする
        runOnUiThread {
            updateNfcIdm(idmString)
        }
    }
    // vvv nfc vvv

    // mode = true (state of reader is ON)
    //      = false (state of reader is OFF)
    override fun toggleNfcReaderState(mode: Boolean) {
        val nfcReaderOn: Button = findViewById(R.id.main_content_nfc_reader_on)
        val nfcReaderOff: Button = findViewById(R.id.main_content_nfc_reader_off)

        when(mode) {
            true -> {
                nfcReaderOn.isEnabled = false
                nfcReaderOff.isEnabled = true

            }
            false -> {
                nfcReaderOn.isEnabled = true
                nfcReaderOff.isEnabled = false
            }
        }
    }

    override fun updateNfcIdm(text: String) {
        val messageView: TextView = findViewById(R.id.main_content_nfc_idm)
        messageView.text = text
    }

    override fun updateMainContentText(text: String) {
        val messageView: TextView = findViewById(R.id.main_content_text)
        messageView.text = text
    }

    override fun handleOkButton(view: View) {
        Snackbar.make(view, "Tapped ${this.counter} times.", Snackbar.LENGTH_SHORT)
            .setAction("Action", null).show()
    }

    override fun restCounter() {
        this.counter = 0
    }

    override fun handleSuccess(result: Array<SinglePostResponse>) {
        Log.d(tTAG, "SUCCESS")
    }

    override fun handleError(message: String) {
        Log.e(tTAG, "ERROR")
    }

    private fun requestWriteExtStoragePermission(manifestPermission: String, requestCode: Int) {
        if (checkSelfPermission(manifestPermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(manifestPermission), requestCode)
        }
    }

}

