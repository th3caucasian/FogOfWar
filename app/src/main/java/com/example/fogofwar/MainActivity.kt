package com.example.fogofwar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.fogofwar.databinding.ActivityMainBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity(), MapListener, SensorEventListener {
    private lateinit var mMap: MapView
    private lateinit var controller: IMapController
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay
    private lateinit var mUpperOverlay: UpperOverlay
    private lateinit var currentIcon: Bitmap
    private lateinit var scaledIcon: Bitmap
    private lateinit var sensorManager: SensorManager
    private var azimuth = 0F
    private var prevAzimuth = 0F
    private var kFilter = KalmanFilter()
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
        controller = mMap.controller
        controller.setZoom(18.0)

        mUpperOverlay = UpperOverlay()
        mMap.overlays.add(mUpperOverlay)

        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)
        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled        // ???
        mMyLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                val location = mMyLocationOverlay.myLocation
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                controller.setCenter(location)
                controller.animateTo(location)
                mUpperOverlay.addClearedTile(geoPoint)
            }
        }
        currentIcon = BitmapFactory.decodeResource(resources, R.drawable.location_arrow_2)        // Если использовать эту переменную - иконка не отображается (видимо дело в размере)
        scaledIcon = Bitmap.createScaledBitmap(currentIcon, 100, 100, true)  // Если использовать эту - то всё отлично работает
        mMyLocationOverlay.setPersonIcon(scaledIcon)
        mMyLocationOverlay.setDirectionIcon(scaledIcon)
        mMyLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        mMyLocationOverlay.setDirectionAnchor(0.5F, 0.5F)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)


        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(this)

    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        Log.e("TAG", "onCreate:la ${event?.source?.mapCenter?.latitude}")
        Log.e("TAG", "onCreate:lo ${event?.source?.mapCenter?.longitude}")
        return true
    }

    override fun onZoom(event:ZoomEvent?): Boolean {
        Log.e("TAG", "onZoom zoom level: ${event?.zoomLevel} source ${event?.source}")
        return false
    }

    // Функция - логика нажатия кнопки возвращения к своей локации
    fun toMyLocationButton(view: View?) {
        runOnUiThread {
            controller.animateTo(mMyLocationOverlay.myLocation)
        }
        controller.setZoom(18.0)
    }

    fun updateLocationButton(view: View?) {
        runOnUiThread {
            val location = mMyLocationOverlay.myLocation
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            controller.setCenter(location)
            controller.animateTo(location)
            mUpperOverlay.addClearedTile(geoPoint)
        }
    }

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)
    private val smoothing = 15      // сглаживание значений азимута (чем больше - тем сильнее сглаживание)
    private var azimuths = mutableListOf<Float>()   // массив значений азимута
    private var avgAzimuth = 0F     // итоговое сглаженное значение азимута

    // Функция обновления поворота иконки (стрелки пользователя)
    private fun updateIconRotation(inAzimuth: Float) {
        azimuths.add(inAzimuth)
        if (azimuths.size > smoothing)
            azimuths.removeAt(0)
        avgAzimuth = azimuths.sum() / azimuths.size
        val rotMatrix = Matrix()
        rotMatrix.postRotate(inAzimuth, scaledIcon.width / 2F, scaledIcon.height / 2F)
        Log.e("AZIMUTH", "Azimuth: ${avgAzimuth}")
        val rotatedBitmap = Bitmap.createBitmap(scaledIcon,0,0,scaledIcon.width, scaledIcon.height, rotMatrix,true)
        mMyLocationOverlay.setPersonIcon(rotatedBitmap)
        mMyLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        mMap.invalidate()
    }


    // Функция, отслеживающая данные с датчиков, обновляющая азимут и вызвыающая функцию обновления поворота иконки пользователя
    override fun onSensorChanged(event: SensorEvent?) {
        when(event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
        }
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic))
        {
            SensorManager.getOrientation(rotationMatrix, orientation)
            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            //val filteredAzimuth = kFilter.update(azimuth)
            updateIconRotation(azimuth)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // not used in application
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI)
    }
}
