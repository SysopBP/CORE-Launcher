package app.lawnchair.hotseat

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.lifecycle.lifecycleScope
import app.lawnchair.launcherNullable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Optional image material for the dock, decoded off the UI thread with a bounded size. */
object DockBackgrounds {
    @JvmStatic
    fun apply(view: View, color: Int, opacity: Int, radius: Float,
        left: Int, top: Int, right: Int, bottom: Int,
        image: String, blur: Boolean, shadow: Boolean) {
        val scope = view.context.launcherNullable?.lifecycleScope ?: return
        scope.launch {
            val bitmap = if (image.isEmpty()) null else withContext(Dispatchers.IO) {
                runCatching {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(view.context.contentResolver, Uri.parse(image))) { decoder, info, _ ->
                        val largest = maxOf(info.size.width, info.size.height)
                        val factor = minOf(1f, 1024f / largest)
                        decoder.setTargetSize(maxOf(1, (info.size.width * factor).toInt()),
                            maxOf(1, (info.size.height * factor).toInt()))
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                }.getOrNull()
            }
            view.background = DockMaterial(bitmap, color, opacity, radius,
                left, top, right, bottom, blur, shadow, view.resources.displayMetrics.density)
        }
    }
}

private class DockMaterial(val image: Bitmap?, val color: Int, val materialOpacity: Int,
    val radius: Float, val left: Int, val top: Int, val right: Int, val bottom: Int,
    val blur: Boolean, val shadow: Boolean, val density: Float) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val panel = RectF()
    private val matrix = Matrix()
    private val imageShader = image?.let { BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
    private var renderNode: RenderNode? = null
    private var drawableAlpha = 255

    override fun draw(canvas: Canvas) {
        panel.set(bounds.left + left.toFloat(), bounds.top + top.toFloat(),
            bounds.right - right.toFloat(), bounds.bottom - bottom.toFloat())
        if (panel.width() <= 0 || panel.height() <= 0) return
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.alpha = drawableAlpha
        if (shadow) {
            paint.color = 0x40000000
            paint.setShadowLayer(10f * density, 0f, 4f * density, 0x66000000)
            canvas.drawRoundRect(panel, radius, radius, paint)
            paint.clearShadowLayer()
        }
        if (image != null && imageShader != null) {
            val scale = maxOf(panel.width() / image.width, panel.height() / image.height)
            matrix.setScale(scale, scale)
            matrix.postTranslate(panel.centerX() - image.width * scale / 2,
                panel.centerY() - image.height * scale / 2)
            imageShader.setLocalMatrix(matrix)
            paint.shader = imageShader
            paint.alpha = materialOpacity * drawableAlpha / 255
            if (blur && Build.VERSION.SDK_INT >= 31 && canvas.isHardwareAccelerated) {
                val node = renderNode ?: RenderNode("CORE dock image").also { renderNode = it }
                val nodeLeft = kotlin.math.floor(minOf(bounds.left.toFloat(), panel.left)).toInt()
                val nodeTop = kotlin.math.floor(minOf(bounds.top.toFloat(), panel.top)).toInt()
                val nodeRight = kotlin.math.ceil(maxOf(bounds.right.toFloat(), panel.right)).toInt()
                val nodeBottom = kotlin.math.ceil(maxOf(bounds.bottom.toFloat(), panel.bottom)).toInt()
                node.setPosition(nodeLeft, nodeTop, nodeRight, nodeBottom)
                node.setRenderEffect(RenderEffect.createBlurEffect(14f * density, 14f * density, Shader.TileMode.CLAMP))
                val recording = node.beginRecording()
                recording.translate(-nodeLeft.toFloat(), -nodeTop.toFloat())
                recording.drawRoundRect(panel, radius, radius, paint)
                node.endRecording()
                val save = canvas.save()
                val clip = android.graphics.Path().apply { addRoundRect(panel, radius, radius, android.graphics.Path.Direction.CW) }
                canvas.clipPath(clip)
                canvas.drawRenderNode(node)
                canvas.restoreToCount(save)
            } else {
                canvas.drawRoundRect(panel, radius, radius, paint)
            }
        }
        paint.shader = null
        paint.color = color
        // An image gets a light tint; the opacity slider controls the overall material.
        paint.alpha = (if (image == null) materialOpacity else materialOpacity / 4) * drawableAlpha / 255
        canvas.drawRoundRect(panel, radius, radius, paint)
    }

    override fun setAlpha(alpha: Int) { drawableAlpha = alpha; invalidateSelf() }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }
    @Deprecated("Deprecated in Android")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}
