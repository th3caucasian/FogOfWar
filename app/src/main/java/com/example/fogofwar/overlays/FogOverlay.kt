package com.example.fogofwar.overlays

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.pow
import kotlin.math.sqrt

class FogOverlay(): Overlay() {
    private val clearedTiles: MutableList<GeoPoint> = mutableListOf()
    private val baseRadius = 50.0

    fun addClearedTile(geoPoint: GeoPoint) {
        clearedTiles.add(geoPoint)
    }

    override fun draw(canvas: Canvas?, mapView: MapView, shadow: Boolean) {
        super.draw(canvas, mapView, shadow)

        if (canvas == null) return
        val fixedPointRadius = mapView.projection.metersToEquatorPixels(15.0f)

        val paint = Paint(). apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            alpha = 215
        }

        val clearPaint = Paint().apply {
            color = Color.TRANSPARENT
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            maskFilter = BlurMaskFilter(fixedPointRadius, BlurMaskFilter.Blur.NORMAL)
        }

        val layer = canvas.saveLayer(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)

        val rect = Rect(0, 0, canvas.width, canvas.height)
        canvas.drawRect(rect, paint)


        for (geoPoint in clearedTiles) {
            val screenPoint = Point()
            mapView.projection.toPixels(geoPoint, screenPoint)
            canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), fixedPointRadius, clearPaint)
        }

        canvas.restoreToCount(layer)
    }

}