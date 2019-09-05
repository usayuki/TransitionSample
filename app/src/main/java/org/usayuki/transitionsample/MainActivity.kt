package org.usayuki.transitionsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.app.ActivityOptions
import android.util.Pair
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView = findViewById(R.id.imageView) as ImageView
        imageView.setOnClickListener {
            val intent = Intent(this, PreviewActivity::class.java)
            intent.putExtra("image", R.drawable.cat)
            val options = ActivityOptions.makeSceneTransitionAnimation(this, Pair.create(imageView, "image"))
            startActivity(intent, options.toBundle())
        }

        val imageView2 = findViewById(R.id.imageView2) as ImageView
        imageView2.setOnClickListener {
            val intent = Intent(this, PreviewActivity::class.java)
            intent.putExtra("image", R.drawable.tarot)
            val options = ActivityOptions.makeSceneTransitionAnimation(this, Pair.create(imageView2, "image"))
            startActivity(intent, options.toBundle())
        }
    }
}
