package org.usayuki.transitionsample.Transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.transition.Transition
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.animation.ObjectAnimator

class CustomTransition : Transition {
    private val PROPERTY_BOUNDS = "transition:bounds"
    private val PROPERTY_POSITION = "transition:position"
    private val PROPERTY_IMAGE = "transition:image"
    private val TRANSITION_PROPERTIES = arrayOf(PROPERTY_BOUNDS, PROPERTY_POSITION)

    constructor() {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun getTransitionProperties(): Array<String> {
        return TRANSITION_PROPERTIES
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view
        transitionValues.values[PROPERTY_BOUNDS] = Rect(view.left, view.top, view.right, view.bottom)
        val position = IntArray(2)
        transitionValues.view.getLocationInWindow(position)
        transitionValues.values[PROPERTY_POSITION] = position
    }

    override fun captureEndValues(transitionValues: TransitionValues?) {
        transitionValues?.let {
            val view = transitionValues.view
            if (view.width <= 0 || view.height <= 0) {
                return
            }
            captureValues(transitionValues)
        }
    }

    override fun captureStartValues(transitionValues: TransitionValues?) {
        transitionValues?.let {
            val view = transitionValues.view
            if (view.width <= 0 || view.height <= 0) {
                return
            }
            captureValues(transitionValues)
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            transitionValues.values[PROPERTY_IMAGE] = bitmap
        }
    }

    override fun createAnimator(
        sceneRoot: ViewGroup?,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator {
        if (startValues == null || endValues == null) {
            return AnimatorSet()
        }

        val startBounds = startValues.values[PROPERTY_BOUNDS] as Rect
        val endBounds = endValues.values[PROPERTY_BOUNDS] as Rect
        if (startBounds == null || endBounds == null || startBounds == endBounds) {
            return AnimatorSet()
        }

        val startImage = startValues.values[PROPERTY_IMAGE] as Bitmap
        val startView = addViewToOverlay(sceneRoot!!, startImage.width, startImage.height)

        val sceneRootLocation = IntArray(2)
        sceneRoot!!.getLocationInWindow(sceneRootLocation)
        val startLocation = startValues.values[PROPERTY_POSITION] as IntArray
        val startTransitionX = (startLocation[0] - sceneRootLocation[0]).toFloat()
        val startTransitionY = (startLocation[1] - sceneRootLocation[1]).toFloat()

        startView.translationX = startTransitionX
        startView.translationY = startTransitionY

        val endView = endValues.view
        endView.alpha = 0f

        val endLocation = endValues.values[PROPERTY_POSITION] as IntArray
        val endTransitionX = (endLocation[0] - sceneRootLocation[0]).toFloat()
        val endTransitionY = (endLocation[1] - sceneRootLocation[1]).toFloat()

        val moveStartView = ObjectAnimator.ofFloat(
            startView, View.TRANSLATION_X, View.TRANSLATION_Y,
            pathMotion.getPath(startTransitionX, startTransitionY, endTransitionX, endTransitionY)
        )

        val animatorSet = AnimatorSet()
        animatorSet.play(moveStartView)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                startView.alpha = 0f
                endView.alpha = 1f
            }

            override fun onAnimationStart(animation: Animator?) {
                startView.alpha = 1f
                endView.alpha = 0f
            }
        })
        return animatorSet
    }

    private fun addViewToOverlay(sceneRoot: ViewGroup, width: Int, height: Int): View {
        val view = NoOverlapView(sceneRoot.context)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, width, height)
        sceneRoot.overlay.add(view)
        return view
    }

    private class NoOverlapView(context: Context) : View(context) {
        override fun hasOverlappingRendering(): Boolean {
            return false
        }
    }
}