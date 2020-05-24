package com.ryoyakawai.felicareader

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.ryoyakawai.felicareader.api.response.SinglePostResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.ryoyakawai.felicareader.libs.FelicaReader

class MainActivity : AppCompatActivity(),
    MainActivityViewContract {

    private var mPresenter: MainActivityPresenterContract? = null
    private var counter: Int = 0
    private val tTAG = "ryoyakawai_falicareader"
    private val reqCodeWriteExternalStorage = 123
    private var permissionGrantedBehavior: ((activity: MainActivity) -> Unit) = {}
    private var permissionDeniedBehavior: ((activity: MainActivity) -> Unit ) = {}
    private var permissionUndefinedBehavior: ((activity: MainActivity) -> Unit ) = {}

    // // vvvv felica vvvv
    private var intentFiltersArray: Array<IntentFilter>? = null
    // FelicaはNFC-TypeFなのでNfcFのみ指定
    private val techListsArray = arrayOf(arrayOf(NfcF::class.java.name))
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private var pendingIntent: PendingIntent? = null
    private val felicaReader = FelicaReader()
    // // ^^ ^^ felica ^^ ^^

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.mPresenter = MainActivityPresenter()
        this.mPresenter!!.setView(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar)

        updateMainContentText("Hello World!!")

        // // vvvv felica vvvv
        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )

        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)

        try {
            ndef.addDataType("text/plain")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("fail", e)
        }

        intentFiltersArray = arrayOf(ndef)

        if (nfcAdapter != null) {
            //NFCが搭載されてない端末
            val builder = AlertDialog.Builder(this@MainActivity, R.style.NfcAlertDialogStyle00)
            builder.setMessage("サービス対象外です")
            builder.setPositiveButton("キャンセル", null)
        } else if (!nfcAdapter!!.isEnabled) {
            //NFCが無効になっている時
            val builder = AlertDialog.Builder(this@MainActivity, R.style.NfcAlertDialogStyle00)
            builder.setTitle("NFC無効")
            builder.setMessage("NFCを有効にしてください")
            builder.setPositiveButton("設定") { _, _ -> startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
            builder.setNegativeButton("キャンセル", null)

            val myDialog = builder.create()
            //ダイアログ画面外をタッチされても消えないようにする。
            myDialog.setCanceledOnTouchOutside(false)
            //ダイアログ表示
            myDialog.show()
        }
        getTag(intent)
        // // ^^^^ Felica ^^^^

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

    private fun getTag(intent: Intent) {
        // IntentにTagの基本データが入ってくるので取得。
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
        felicaReader.readTag(tag, applicationContext)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getTag(intent)
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

