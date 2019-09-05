package org.usayuki.transitionsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import com.github.chrisbanes.photoview.OnScaleChangedListener
import com.github.chrisbanes.photoview.OnSingleFlingListener
import com.github.chrisbanes.photoview.PhotoView

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val image = intent.getIntExtra("image", R.drawable.cat)
        val imageView = findViewById(R.id.imageView) as PhotoView
        imageView.setImageResource(image)
        imageView.minimumScale = 1f
        imageView.maximumScale = 2f
        imageView.setOnScaleChangeListener(object : OnScaleChangedListener {
            override fun onScaleChange(scaleFactor: Float, focusX: Float, focusY: Float) {
                if (imageView.scale < 0.5f) {
                    finishAfterTransition()
                }
            }
        })
        imageView.setOnSingleFlingListener(object : OnSingleFlingListener {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (imageView.scale == 1f) {
                    finishAfterTransition()
                    return true
                }
                return false
            }
        })

        val backButton = findViewById(R.id.button) as Button
        backButton.setOnClickListener {
            finishAfterTransition()
        }
    }
}