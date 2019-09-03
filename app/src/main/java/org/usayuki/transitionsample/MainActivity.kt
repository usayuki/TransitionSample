package org.usayuki.transitionsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView1 = findViewById(R.id.imageView1) as ImageView
        imageView1.setOnClickListener {
            nextView(R.drawable.cat)
        }

        val imageView2 = findViewById(R.id.imageView2) as ImageView
        imageView2.setOnClickListener {
            nextView(R.drawable.tarot)
        }
    }

    private fun nextView(image: Int) {
        val intent = Intent(this, PreviewActivity::class.java)
        intent.putExtra("image", image)
        startActivity(intent)
    }
}
