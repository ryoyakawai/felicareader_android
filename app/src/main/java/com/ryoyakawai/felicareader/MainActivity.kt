package com.ryoyakawai.felicareader

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    private val tTAG = "ryoyakawai_falicareader"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // set initial fragment
        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragment_container, MainActivityViewFragment())
            transaction.commit()
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        createFloatMenu(R.menu.menu_main, menu)
        return true
    }

    private fun createFloatMenu(id: Int, menu: Menu) {
        menuInflater.inflate(R.menu.menu_main, menu)
    }

    private fun requestWriteExtStoragePermission(manifestPermission: String, requestCode: Int) {
        if (checkSelfPermission(manifestPermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(manifestPermission), requestCode)
        }
    }

}

