package com.threeal.smarttourism

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.*

class LoginActivity : AppCompatActivity() {

    private var scannerView: CodeScannerView? = null
    private var tagIdEdit: EditText? = null
    private var loginButton: Button? = null

    private var codeScanner: CodeScanner? = null

    private var tagId: String? = null

    private val loginOnClickListener = View.OnClickListener {
        Toast.makeText(baseContext, R.string.login_trying, Toast.LENGTH_SHORT).show()
        tagIdEdit?.let { safeTagIdEdit ->
            tagId = safeTagIdEdit.text.toString()
            tagId?.let { safeTagId ->
                Place.fetchPlaces(this, safeTagId, true)
            }
        }
    }

    private val placeListener = PlaceListener { places ->
        if (places != null) {
            val intent = Intent(baseContext, ArActivity::class.java).apply {
                putExtra(getString(R.string.intent_tag_id), tagId)
            }

            startActivity(intent)
        } else {
            Toast.makeText(this, R.string.tag_id_invalid, Toast.LENGTH_SHORT).show()
        }
    }

    private val scannerDecodeCallback = DecodeCallback {
        runOnUiThread {
            tagIdEdit?.setText(it.text)

            Toast.makeText(
                this,
                getString(R.string.detect_tag_id).format(it.text),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val scannerErrorCallback = ErrorCallback {
        runOnUiThread {
            Toast.makeText(this, getString(R.string.scanner_error), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        scannerView = findViewById(R.id.scannerView)
        tagIdEdit = findViewById(R.id.tagIdEdit)
        loginButton = findViewById(R.id.loginButton)

        scannerView?.let {
            codeScanner = CodeScanner(this, it).apply {
                camera = CodeScanner.CAMERA_BACK
                formats = CodeScanner.ALL_FORMATS

                autoFocusMode = AutoFocusMode.SAFE
                scanMode = ScanMode.SINGLE
                isAutoFocusEnabled = true
                isFlashEnabled = false
            }
        }
    }

    override fun onResume() {
        super.onResume()

        loginButton?.setOnClickListener(loginOnClickListener)

        PlaceListener.register(placeListener)

        codeScanner?.apply {
            decodeCallback = scannerDecodeCallback
            errorCallback = scannerErrorCallback
        }

        scannerView?.setOnClickListener {
            codeScanner?.startPreview()
        }

        codeScanner?.startPreview()
    }

    override fun onPause() {
        super.onPause()

        PlaceListener.unregister(placeListener)

        codeScanner?.releaseResources()
    }
}