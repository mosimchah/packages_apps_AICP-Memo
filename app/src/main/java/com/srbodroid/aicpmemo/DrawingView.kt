package com.srbodroid.aicpmemo

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

import java.io.File

/**
 * Created by eboye on 1/11/16.
 *
 */
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //drawing path
    private var drawPath: Path? = null
    //drawing and canvas paint
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    //initial color
    private var paintColor = -0xc7afaa
    //canvas
    private var drawCanvas: Canvas? = null
    //canvas bitmap
    private var canvasBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
    // brush size
    private var brushSize: Float = 0.toFloat()
    var lastBrushSize: Float = 0.toFloat()
    // cursor
    private var cursor: Cursor? = null

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        //get drawing area setup for interaction
        drawPath = Path()
        drawPaint = Paint()
        drawPaint!!.color = paintColor
        drawPaint!!.isAntiAlias = true
        drawPaint!!.strokeWidth = 20f
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)

        brushSize = 20f
        lastBrushSize = brushSize
        drawPaint!!.strokeWidth = brushSize
    }

    override fun onSizeChanged(w: Int, h: Int, oldWidth: Int, oldHeight: Int) {
        //view given size
        super.onSizeChanged(w, h, oldWidth, oldHeight)

        val defaultBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        if (canvasBitmap.sameAs(defaultBitmap)) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        } else {
            Log.d("BitmapSize", "width" + w.toString())
            Log.d("BitmapSize", "height" + h.toString())
            canvasBitmap = getResizedBitmap(canvasBitmap, w, h)
        }
        drawCanvas = Canvas(canvasBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        //draw view
        canvas.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)
        canvas.drawPath(drawPath!!, drawPaint!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //detect user touch
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> drawPath!!.moveTo(touchX, touchY)
            MotionEvent.ACTION_MOVE -> drawPath!!.lineTo(touchX, touchY)
            MotionEvent.ACTION_UP -> {
                drawCanvas!!.drawPath(drawPath!!, drawPaint!!)
                drawPath!!.reset()
            }
            else -> return false
        }

        invalidate()
        return true
    }

    fun setColor(newColor: String) {
        //set color
        invalidate()
        paintColor = Color.parseColor(newColor)
        drawPaint!!.color = paintColor
    }

    fun setBrushSize(newSize: Float) {
        //update size
        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, resources.displayMetrics)
        drawPaint!!.strokeWidth = brushSize
    }

    fun setErase(isErase: Boolean) {
        //set erase true or false
        if (isErase)
            drawPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        else
            drawPaint!!.xfermode = null
    }

    fun startNew(color: Int) {
        drawCanvas!!.drawColor(color)
        invalidate()
    }

    fun placeImage(uri: Uri) {
        val file = File(getRealPathFromURI(uri))
        //cursor.close();
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val loadedBitmap = BitmapFactory.decodeFile(file.absolutePath, options)
        val bitmap = loadedBitmap.copy(Bitmap.Config.ARGB_8888, true)
        if (bitmap != null) {
            canvasBitmap = bitmap
            invalidate()
        }
    }

    private fun getRealPathFromURI(contentURI: Uri): String {
        cursor = context.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.path
        } else {
            cursor!!.moveToFirst()
            val idx = cursor!!.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            return cursor!!.getString(idx)
        }
    }

    fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap {

        val width = bm.width
        val height = bm.height

        val scale = (newWidth / width).toFloat()
        val xTranslation = 0.0f
        val yTranslation = (newHeight - height * scale) / 2.0f
        val transformation = Matrix()
        transformation.postTranslate(xTranslation, yTranslation)
        transformation.preScale(scale, scale)

        return Bitmap.createBitmap(bm, 0, 0, width, height, transformation, true)

    }

}
