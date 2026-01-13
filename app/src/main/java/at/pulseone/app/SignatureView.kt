package at.pulseone.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView

class SignatureView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 12f // Increased stroke width for better visibility
    }

    private val path = Path()
    private val signatureBounds = RectF()

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.TRANSPARENT)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                if (signatureBounds.isEmpty) {
                    signatureBounds.set(x, y, x, y)
                } else {
                    signatureBounds.union(x, y)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                signatureBounds.union(x, y)
            }
            else -> return false
        }

        invalidate()
        return true
    }

    fun clear() {
        path.reset()
        signatureBounds.setEmpty()
        invalidate()
    }

    fun isBlank(): Boolean {
        return path.isEmpty
    }

    fun getSignatureBitmap(): Bitmap? {
        if (signatureBounds.isEmpty) return null

        val bitmap = Bitmap.createBitmap(signatureBounds.width().toInt(), signatureBounds.height().toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val translatedPath = Path(path)
        val matrix = android.graphics.Matrix()
        matrix.setTranslate(-signatureBounds.left, -signatureBounds.top)
        translatedPath.transform(matrix)

        canvas.drawPath(translatedPath, paint)
        return bitmap
    }

    fun getSignaturePlacement(pdfPreview: ImageView, originalPdfWidth: Int, originalPdfHeight: Int): RectF {
        val imageDrawable = pdfPreview.drawable ?: return RectF()

        val imageMatrix = pdfPreview.imageMatrix
        val drawableRect = RectF(0f, 0f, imageDrawable.intrinsicWidth.toFloat(), imageDrawable.intrinsicHeight.toFloat())
        val viewRect = RectF()
        imageMatrix.mapRect(viewRect, drawableRect)

        val relativeLeft = (signatureBounds.left - viewRect.left) / viewRect.width()
        val relativeTop = (signatureBounds.top - viewRect.top) / viewRect.height()
        val relativeRight = (signatureBounds.right - viewRect.left) / viewRect.width()
        val relativeBottom = (signatureBounds.bottom - viewRect.top) / viewRect.height()

        return RectF(
            relativeLeft * originalPdfWidth,
            relativeTop * originalPdfHeight,
            relativeRight * originalPdfWidth,
            relativeBottom * originalPdfHeight
        )
    }
}