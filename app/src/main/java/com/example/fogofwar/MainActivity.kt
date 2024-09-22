package com.example.fogofwar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.location.GpsStatus
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.fogofwar.databinding.ActivityMainBinding
import com.google.android.gms.maps.model.*
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MainActivity : AppCompatActivity(), MapListener, GpsStatus.Listener {
    lateinit var mMap: MapView
    lateinit var controller: IMapController
    lateinit var mMyLocationOverlay: MyLocationNewOverlay
    lateinit var mButton: Button
    lateinit var currentIcon: Bitmap
    lateinit var scaledIcon: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        )
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.mapCenter          // ???
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())

        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)
        controller = mMap.controller

        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled()       // ???
        mMyLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation)
                controller.animateTo(mMyLocationOverlay.myLocation)
            }
        }

        currentIcon = BitmapFactory.decodeResource(resources, R.drawable.location_arrow_2)                // Если использовать эту переменную - иконка не отображается (видимо дело в размере)
        scaledIcon = Bitmap.createScaledBitmap(currentIcon, 50, 50, true)  // Если использовать эту - то всё отлично работает
        if (currentIcon == null)
            Log.e("TAG", "ICON FAIL")
        else
            mMyLocationOverlay.setPersonIcon(scaledIcon)


        controller.setZoom(18.0)
        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(this)
        Log.e("TAG", "onCreate:in ${controller.zoomIn()}")
        Log.e("TAG", "onCreate:out ${controller.zoomOut()}")
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        Log.e("TAG", "onCreate:la ${event?.source?.getMapCenter()?.latitude}")
        Log.e("TAG", "onCreate:lo ${event?.source?.getMapCenter()?.longitude}")
        return true
    }

    override fun onZoom(event:ZoomEvent?): Boolean {
        Log.e("TAG", "onZoom zoom level: ${event?.zoomLevel} source ${event?.source}")
        return false;
    }

    override fun onGpsStatusChanged(event: Int) {
        TODO("Not yet implemented")
    }

    fun toMyLocationButton(view: View?) {
        runOnUiThread {
            controller.animateTo(mMyLocationOverlay.myLocation)
        }
        controller.setZoom(18.0)
    }

}
