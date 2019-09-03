package org.usayuki.transitionsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.app.ActivityOptions
import android.util.Pair
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView = findViewById(R.id.imageView) as ImageView
        imageView.setOnClickListener {
            nextView(R.drawable.cat)
        }
    }

    private fun nextView(image: Int) {
        val intent = Intent(this, PreviewActivity::class.java)
        intent.putExtra("image", image)
        val options = ActivityOptions.makeSceneTransitionAnimation(this, Pair.create(imageView as View, "cover"), Pair.create(imageView as View, "base"))
        startActivity(intent, options.toBundle())
    }
}
