package app.lawnchair.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Color
import android.util.AttributeSet
import app.lawnchair.launcherNullable
import app.lawnchair.preferences.PreferenceManager
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.util.SystemUiController
import com.android.launcher3.util.Themes
import com.android.launcher3.views.ScrimView

class LawnchairScrimView(context: Context, attrs: AttributeSet?) : ScrimView(context, attrs) {

    private var drawerOpacity = 0f

    init {
        val pm = PreferenceManager.getInstance(context)
        pm.drawerOpacity.subscribeValues(this) {
            drawerOpacity = it
        }
    }

    private val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glassBounds = RectF()

    private var glassShader: Shader? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            glassShader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(0x25FFFFFF, 0x05FFFFFF, 0x12000000),
                floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (PreferenceManager.getInstance(context).glassAppDrawer.get() &&
            context.launcherNullable?.isInState(LauncherState.ALL_APPS) == true) {
            val density = resources.displayMetrics.density
            val inset = 8f * density
            glassBounds.set(inset, inset, width - inset, height - inset)
            val radius = 32f * density
            glassPaint.style = Paint.Style.FILL
            glassPaint.shader = glassShader
            canvas.drawRoundRect(glassBounds, radius, radius, glassPaint)
            glassPaint.shader = null
            glassPaint.style = Paint.Style.STROKE
            glassPaint.strokeWidth = density
            glassPaint.color = 0x45FFFFFF
            canvas.drawRoundRect(glassBounds, radius, radius, glassPaint)
        }
        super.onDraw(canvas)
    }

    override fun updateSysUiColors() {
        val threshold = STATUS_BAR_COLOR_FORCE_UPDATE_THRESHOLD
        val forceChange = visibility == VISIBLE &&
            alpha > threshold &&
            Color.alpha(mBackgroundColor) / (255f * drawerOpacity) > threshold
        with(systemUiController) {
            if (forceChange) {
                updateUiState(SystemUiController.UI_STATE_SCRIM_VIEW, !isScrimDark)
            } else {
                updateUiState(SystemUiController.UI_STATE_SCRIM_VIEW, 0)
            }
        }
    }

    override fun isScrimDark() = if (drawerOpacity <= 0.3f) {
        !Themes.getAttrBoolean(context, R.attr.isWorkspaceDarkText)
    } else {
        super.isScrimDark()
    }
}
