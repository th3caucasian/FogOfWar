package com.example.fogofwar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class UpperOverlay: Overlay() {
    private val clearedTiles: MutableList<GeoPoint> = mutableListOf()

    fun addClearedTile(geoPoint: GeoPoint) {
        clearedTiles.add(geoPoint)
    }

    override fun draw(canvas: Canvas?, mapView: MapView, shadow: Boolean) {
        super.draw(canvas, mapView, shadow)

        if (canvas != null && mapView != null)
        {
            val paint = Paint()
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL

            for (point in clearedTiles) {
                val screenPoint = Point()
                mapView.projection.toPixels(point, screenPoint)

                val rect = Rect(screenPoint.x - 30, screenPoint.y - 30, screenPoint.x + 30, screenPoint.y + 30)
                canvas.drawRect(rect, paint)
            }

        }

    }



}