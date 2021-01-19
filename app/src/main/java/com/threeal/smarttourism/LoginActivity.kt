package com.threeal.smarttourism

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private var tagIdEdit: EditText? = null
    private var loginButton: Button? = null

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
                putExtra("com.threeal.smarttourism.TAG_ID", tagId)
            }

            startActivity(intent)
        } else {
            Toast.makeText(
                baseContext,
                R.string.tag_id_invalid,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tagIdEdit = findViewById(R.id.tagIdEdit)
        loginButton = findViewById(R.id.loginButton)
    }

    override fun onStart() {
        super.onStart()

        loginButton?.setOnClickListener(loginOnClickListener)

        PlaceListener.register(placeListener)
    }

    override fun onPause() {
        super.onPause()

        PlaceListener.unregister(placeListener)
    }
}