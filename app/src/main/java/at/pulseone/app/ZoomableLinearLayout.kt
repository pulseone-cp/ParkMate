package at.pulseone.app

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.LinearLayout

class ZoomableLinearLayout(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var scaleMatrix = Matrix()
    private var inverseMatrix = Matrix()

    private var scaleFactor = 1.0f
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f

    init {
        setWillNotDraw(false)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(ev)
        gestureDetector.onTouchEvent(ev)

        val action = ev.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = ev.x
                lastTouchY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress) {
                    val x = ev.x
                    val y = ev.y
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY

                    posX += dx
                    posY += dy

                    lastTouchX = x
                    lastTouchY = y

                    invalidate()
                }
            }
        }
        return true
    }

    override fun dispatchDraw(canvas: android.graphics.Canvas) {
        canvas.save()
        canvas.translate(posX, posY)
        canvas.scale(scaleFactor, scaleFactor)
        super.dispatchDraw(canvas)
        canvas.restore()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val oldScale = scaleFactor
            scaleFactor *= detector.scaleFactor
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 5.0f))

            val focusX = detector.focusX
            val focusY = detector.focusY

            posX -= (focusX - posX) * (scaleFactor / oldScale - 1)
            posY -= (focusY - posY) * (scaleFactor / oldScale - 1)

            invalidate()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (scaleFactor > 1.0f) {
                scaleFactor = 1.0f
                posX = 0f
                posY = 0f
            } else {
                scaleFactor = 2.0f
            }
            invalidate()
            return true
        }
    }
}
