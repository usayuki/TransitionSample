package org.usayuki.transitionsample.Transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.transition.Transition
import android.transition.TransitionValues
import android.util.ArrayMap
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import org.usayuki.transitionsample.R

import java.util.ArrayList
import kotlin.math.sqrt

class CustomTransition : Transition {
    private val PROPERTY_BOUNDS = "circleTransition:bounds"
    private val PROPERTY_POSITION = "circleTransition:position"
    private val PROPERTY_IMAGE = "circleTransition:image"
    private val TRANSITION_PROPERTIES = arrayOf(PROPERTY_BOUNDS, PROPERTY_POSITION)

    var color = Color.parseColor("#6c1622")

    constructor() {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.CustomTransition)
        color = attr.getColor(R.styleable.CustomTransition_colorCT, color)
        attr.recycle()
    }

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
        val startBackground = BitmapDrawable(startImage)
        val startView = addViewToOverlay(sceneRoot!!, startImage.width, startImage.height, startBackground)
        val shrinkingBackground = ColorDrawable(color)
        val shrinkingView = addViewToOverlay(sceneRoot!!, startImage.width, startImage.height, shrinkingBackground)

        val sceneRootLocation = IntArray(2)
        sceneRoot!!.getLocationInWindow(sceneRootLocation)
        val startLocation = startValues.values[PROPERTY_POSITION] as IntArray
        val startTransitionX = startLocation[0] - sceneRootLocation[0]
        val startTransitionY = startLocation[1] - sceneRootLocation[1]

        startView.translationX = startTransitionX.toFloat()
        startView.translationY = startTransitionY.toFloat()
        shrinkingView.translationX = startTransitionX.toFloat()
        shrinkingView.translationY = startTransitionY.toFloat()

        val endView = endValues.view
        val startRadius = calculateMaxRadius(shrinkingView)
        val minRadius = Math.min(calculateMinRadius(shrinkingView), calculateMinRadius(endView))

        val circleBackground = ShapeDrawable(OvalShape())
        circleBackground.paint.color = color
        val circleView = addViewToOverlay(sceneRoot!!, minRadius * 2, minRadius * 2, circleBackground)
        val circleStartX = (startLocation[0] - sceneRootLocation[0] + (startView.width - circleView.width) / 2).toFloat()
        val circleStartY = (startLocation[1] - sceneRootLocation[1] + (startView.height - circleView.height) / 2).toFloat()
        circleView.translationX = circleStartX
        circleView.translationY = circleStartY

        circleView.visibility = View.INVISIBLE
        shrinkingView.alpha = 0f
        endView.alpha = 0f

        val shrinkingAnimator = createCircularReveal(shrinkingView, startRadius, minRadius.toFloat())
        shrinkingAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                shrinkingView.visibility = View.INVISIBLE
                startView.visibility = View.INVISIBLE
                circleView.visibility = View.INVISIBLE
            }
        })

        val startAnimator = createCircularReveal(startView, startRadius, minRadius.toFloat())
        val fadeInAnimator = ObjectAnimator.ofFloat(shrinkingView, "alpha", 1f, 0f)

        val shrinkFadeSet = AnimatorSet()
        shrinkFadeSet.playTogether(shrinkingAnimator, startAnimator, fadeInAnimator)

        val endLocation = endValues.values[PROPERTY_POSITION] as IntArray
        val circleEndX = (endLocation[0] - sceneRootLocation[0] + (endView.width - circleView.width) / 2).toFloat()
        val circleEndY = (endLocation[1] - sceneRootLocation[1] + (endView.height - circleView.height) / 2).toFloat()
        val circlePath = pathMotion.getPath(circleStartX, circleStartY, circleEndX, circleEndY)
        val circleAnimator = ObjectAnimator.ofFloat(circleView, View.TRANSLATION_X, View.TRANSLATION_Y, circlePath)

        val growingView = addViewToOverlay(sceneRoot!!, endView.width, endView.height, shrinkingBackground)
        growingView.visibility = View.INVISIBLE
        val endTransitionX = (endLocation[0] - sceneRootLocation[0]).toFloat()
        val endTransitionY = (endLocation[1] - sceneRootLocation[1]).toFloat()
        growingView.translationX = endTransitionX
        growingView.translationY = endTransitionY

        val endRadius = calculateMaxRadius(endView)

        circleAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                circleView.visibility = View.INVISIBLE
                growingView.visibility = View.INVISIBLE
                endView.alpha = 1f
            }
        })

        val fadeOutAnimator = ObjectAnimator.ofFloat(growingView, "alpha", 1f, 0f)
        val endAnimator = createCircularReveal(endView, minRadius.toFloat(), endRadius)
        val growingAnimator = createCircularReveal(growingView, minRadius.toFloat(), endRadius)
        growingAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                sceneRoot.overlay.remove(startView)
                sceneRoot.overlay.remove(shrinkingView)
                sceneRoot.overlay.remove(circleView)
                sceneRoot.overlay.remove(growingView)
            }
        })

        val growingFadeSet = AnimatorSet()
        growingFadeSet.playTogether(fadeOutAnimator, endAnimator, growingAnimator)

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(shrinkFadeSet, circleAnimator, growingFadeSet)
        return animatorSet
    }

    private fun addViewToOverlay(sceneRoot: ViewGroup, width: Int, height: Int, background: Drawable): View {
        val view = NoOverlapView(sceneRoot.context)
        view.background = background
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, width, height)
        sceneRoot.overlay.add(view)
        return view
    }

    private fun createCircularReveal(view: View, startRadius: Float, endRadius: Float): Animator {
        val centerX = view.width / 2
        val centerY = view.height / 2
        val reveal = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius, endRadius)
        return NoPauseAnimatior(reveal)
    }

    private class NoPauseAnimatior(private val mAnimator: Animator) : Animator() {
        private val mListeners = ArrayMap<Animator.AnimatorListener, Animator.AnimatorListener>()

        override fun addListener(listener: AnimatorListener?) {
            listener?.let {
                val wrapper = AnimatorListenerWrapper(this, listener)
                if (!mListeners.containsKey(listener)) {
                    mListeners[listener] = wrapper
                    mAnimator.addListener(wrapper)
                }
            }
        }

        override fun cancel() {
            mAnimator.cancel()
        }

        override fun end() {
            mAnimator.end()
        }

        override fun getDuration(): Long {
            return mAnimator.duration
        }

        override fun getInterpolator(): TimeInterpolator {
            return mAnimator.interpolator
        }

        override fun getListeners(): ArrayList<AnimatorListener> {
            return ArrayList(mListeners.keys)
        }

        override fun getStartDelay(): Long {
            return mAnimator.startDelay
        }

        override fun isPaused(): Boolean {
            return mAnimator.isPaused
        }

        override fun isRunning(): Boolean {
            return mAnimator.isRunning
        }

        override fun isStarted(): Boolean {
            return mAnimator.isStarted
        }

        override fun removeAllListeners() {
            mListeners.clear()
            mAnimator.removeAllListeners()
        }

        override fun removeListener(listener: AnimatorListener?) {
            listener?.let {
                val wrapper = mListeners[listener]
                if (wrapper != null) {
                    mListeners.remove(listener)
                    mAnimator.removeListener(wrapper)
                }
            }
        }

        override fun setDuration(duration: Long): Animator {
            mAnimator.duration = duration
            return this
        }

        override fun setInterpolator(value: TimeInterpolator?) {
            mAnimator.interpolator = value
        }

        override fun setStartDelay(startDelay: Long) {
            mAnimator.startDelay = startDelay
        }

        override fun setTarget(target: Any?) {
            mAnimator.setTarget(target)
        }

        override fun setupEndValues() {
            mAnimator.setupEndValues()
        }

        override fun setupStartValues() {
            mAnimator.setupStartValues()
        }

        override fun start() {
            mAnimator.start()
        }
    }

    private class AnimatorListenerWrapper(private val mAnimator: Animator, private val mListener: Animator.AnimatorListener) : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
            mListener.onAnimationStart(mAnimator)
        }

        override fun onAnimationEnd(animation: Animator?) {
            mListener.onAnimationEnd(mAnimator)
        }

        override fun onAnimationCancel(animation: Animator?) {
            mListener.onAnimationCancel(mAnimator)
        }

        override fun onAnimationRepeat(animation: Animator?) {
            mListener.onAnimationRepeat(mAnimator)
        }
    }

    private class NoOverlapView(context: Context) : View(context) {
        override fun hasOverlappingRendering(): Boolean {
            return false
        }
    }

    private fun calculateMaxRadius(view: View): Float {
        val widthSquared = (view.width * view.width).toFloat()
        val heightSquared = (view.height * view.height).toFloat()
        return sqrt(widthSquared + heightSquared) / 2
    }

    private fun calculateMinRadius(view: View): Int {
        return Math.min(view.width / 2, view.height / 2)
    }
}