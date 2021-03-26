package com.mipapadakis.canvas

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.mipapadakis.canvas.ui.canvas.CreateCanvasFragment
private const val IMPORT_IMAGE_INTENT_KEY = CreateCanvasFragment.IMPORT_IMAGE_INTENT_KEY

class CanvasActivity : AppCompatActivity() {
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        toast = Toast(this)
        if(intent==null) showToast("No intent!")
        else{
            val uri = intent.getStringExtra(IMPORT_IMAGE_INTENT_KEY) ?: ""
            findViewById<ImageView>(R.id.canvas_image).setImageURI(Uri.parse(uri))
        }

    }

    private fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(this, text, toast.duration)
        toast.show()
    }
}