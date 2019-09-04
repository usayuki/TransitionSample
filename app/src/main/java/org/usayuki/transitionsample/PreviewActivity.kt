package org.usayuki.transitionsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val image = intent.getIntExtra("image", R.drawable.cat)
        val imageView = findViewById(R.id.imageView) as ImageView
        imageView.setImageResource(image)

        val backButton = findViewById(R.id.button) as Button
        backButton.setOnClickListener {
            finishAfterTransition()
        }
    }
}