package com.example.fogofwar.overlays

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.Log
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class FogOverlay(): Overlay() {
    private val clearedTiles: MutableList<GeoPoint> = mutableListOf()
    private val baseRadius = 50.0

    fun addClearedTile(geoPoint: GeoPoint) {
        clearedTiles.add(geoPoint)
    }

    override fun draw(canvas: Canvas?, mapView: MapView, shadow: Boolean) {
        super.draw(canvas, mapView, shadow)

        if (canvas != null)
        {
            val paint = Paint()
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            paint.alpha = 150
            val rect = Rect(0, 0, canvas.width, canvas.height)
            val layer = canvas.saveLayer(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            canvas.drawRect(rect, paint)

            val zoomLevel = mapView.zoomLevelDouble
            val scaledRadius = ((baseRadius * zoomLevel) / (18 * baseRadius)).toFloat()
            Log.d("ZOOM", "$zoomLevel")

            val clearPaint = Paint()
            clearPaint.color = Color.TRANSPARENT
            clearPaint.style = Paint.Style.FILL
            clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            //clearPaint.maskFilter = BlurMaskFilter(scaledRadius, BlurMaskFilter.Blur.NORMAL)


            for (geoPoint in clearedTiles) {
                val screenPoint = Point()
                mapView.projection.toPixels(geoPoint, screenPoint)
                canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), scaledRadius, clearPaint)
            }
            canvas.restoreToCount(layer)
        }
    }

}