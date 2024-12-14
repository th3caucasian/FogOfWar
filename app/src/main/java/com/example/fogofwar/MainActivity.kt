package com.example.fogofwar

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.features.updatePoints.UpdatePointsReceiveRemote
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.get_points.GetPointsReceiveRemote
import com.example.fogofwar.backend.remotes.register.RegisterReceiveRemote
import com.example.fogofwar.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), MapListener /* SensorEventListener*/ {
    private lateinit var mMap: MapView
    private lateinit var controller: IMapController
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay
    private lateinit var mUpperOverlay: UpperOverlay
    private lateinit var currentIcon: Bitmap
    private lateinit var scaledIcon: Bitmap
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var compassOrientationProvider: InternalCompassOrientationProvider
    private var azimuth = 0F
    private var prevAzimuth = 0F


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
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())
        controller = mMap.controller
        controller.setZoom(18.0)


        currentIcon = BitmapFactory.decodeResource(resources, R.drawable.location_arrow_2)         // Если использовать эту переменную - иконка не отображается (видимо дело в размере)
        scaledIcon = Bitmap.createScaledBitmap(currentIcon, 100, 100, true)  // Если использовать эту - то всё отлично работает
        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)
        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = false
        mMyLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        mMyLocationOverlay.setPersonIcon(scaledIcon)
        mMyLocationOverlay.setPersonAnchor(0.5F, 0.5F)

        // Активация локации (отслеживание перемещения)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setWaitForAccurateLocation(false)
            .setMinUpdateDistanceMeters(1f)
            .setMinUpdateIntervalMillis(1000)
            .build()

        // Активация поворота иконки
        compassOrientationProvider = InternalCompassOrientationProvider(this)
        compassOrientationProvider.startOrientationProvider { azimuth, _ ->
            // Обработка азимута, определяющего текущее направление устройства
            updateIconRotation(azimuth)
        }
//        TODO("Разобраться также")
//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
//        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)


        mUpperOverlay = UpperOverlay()
        mMap.overlays.add(mUpperOverlay)
        mMap.overlays.add(mMyLocationOverlay)
        startLocationUpdates() //mMap.addMapListener(this)

        //-----------------------------------------------  Разобрать
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.69.194:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val backendAPI = retrofit.create(BackendAPI::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val getter = backendAPI.getPoints(GetPointsReceiveRemote("123")).body()?.points!![0].x
            Log.e("GET", "$getter")
        }
        //-----------------------------------------------

    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations)
                    updateMapLocation(location)
            }
        }, null)
    }

    private fun updateMapLocation(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        controller.setCenter(geoPoint)
        controller.animateTo(geoPoint)
        mUpperOverlay.addClearedTile(geoPoint)
        Log.d("LOCATION_UPDATE", "Location: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }

    // Функция - логика нажатия кнопки возвращения к своей локации
    fun toMyLocationButton(view: View?) {
        runOnUiThread {
            controller.animateTo(mMyLocationOverlay.myLocation)
        }
        controller.setZoom(18.0)
    }

//    TODO("Разобраться и убрать переменные")
//    private var gravity = FloatArray(3)
//    private var geomagnetic = FloatArray(3)
//    private var rotationMatrix = FloatArray(9)
//    private var orientation = FloatArray(3)
//    private val smoothing = 15     // сглаживание значений азимута (чем больше - тем сильнее сглаживание)
//    private var azimuths = mutableListOf<Float>()   // массив значений азимута
//    private var avgAzimuth = 0F     // итоговое сглаженное значение азимута

    // Функция обновления поворота иконки (стрелки пользователя)
    private fun updateIconRotation(inAzimuth: Float) {
        // TODO("Разобраться с этими строками")
//        azimuths.add(inAzimuth)
//        if (azimuths.size > smoothing)
//            azimuths.removeAt(0)
//        avgAzimuth = azimuths.sum() / azimuths.size
        val rotMatrix = Matrix()
        rotMatrix.postRotate(inAzimuth, scaledIcon.width / 2F, scaledIcon.height / 2F)
        val rotatedBitmap = Bitmap.createBitmap(scaledIcon,0,0,scaledIcon.width, scaledIcon.height, rotMatrix,true)
        mMyLocationOverlay.setPersonIcon(rotatedBitmap)
        mMyLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        mMap.invalidate()
    }


    // Функция, отслеживающая данные с датчиков, обновляющая азимут и вызвыающая функцию обновления поворота иконки пользователя
    // TODO("Разобаться с этой функцией")
//    override fun onSensorChanged(event: SensorEvent?) {
//        when(event?.sensor?.type) {
//            Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
//            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
//        }
//        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic))
//        {
//            SensorManager.getOrientation(rotationMatrix, orientation)
//            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
//           // updateIconRotation(azimuth)
//        }
//    }

    override fun onPause() {
        super.onPause()
        compassOrientationProvider.stopOrientationProvider()
        //TODO("Разобраться с сенсорами")
        //sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        compassOrientationProvider.startOrientationProvider{azimuth, _ -> updateIconRotation(azimuth)}
        //TODO("Разобраться с сенсорами")
        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI)
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

//    TODO("Если убираю SensorEventListener - это тоже убрать")
//    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//        // not used in application
//    }
}
