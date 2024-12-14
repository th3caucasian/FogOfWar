package com.example.fogofwar

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.fogofwar.additions.Point
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.get_points.GetPointsReceiveRemote
import com.example.fogofwar.databinding.ActivityMainBinding
import com.example.fogofwar.overlays.FogOverlay
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

class MainActivity : AppCompatActivity(), MapListener {
    private lateinit var map: MapView
    private lateinit var controller: IMapController
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var fogOverlay: FogOverlay
    private lateinit var currentIcon: Bitmap
    private lateinit var scaledIcon: Bitmap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var compassOrientationProvider: InternalCompassOrientationProvider
    private lateinit var backendAPI: BackendAPI
    private var userPoints = mutableListOf<Point>()
    private var userClearedPoints = mutableListOf<Point>()
    private var userPhoneNumber = "89880888306"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        )

        setupMap(binding)
        setupLocation()
        setupRetrofit()
        fogOverlay = FogOverlay()
        map.overlays.add(fogOverlay)
        map.overlays.add(myLocationOverlay)

        loadUserClearedPoints()
    }


    private fun setupMap(activityBinding: ActivityMainBinding) {
        map = activityBinding.osmmap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.getLocalVisibleRect(Rect())
        controller = map.controller
        controller.setZoom(18.0)

        currentIcon = BitmapFactory.decodeResource(resources, R.drawable.location_arrow_2)         // Если использовать эту переменную - иконка не отображается (видимо дело в размере)
        scaledIcon = Bitmap.createScaledBitmap(currentIcon, 100, 100, true)  // Если использовать эту - то всё отлично работает

    }



    private fun setupLocation() {
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.isDrawAccuracyEnabled = false
        myLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        myLocationOverlay.setPersonIcon(scaledIcon)

        // Активация локации (отслеживание перемещения)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setWaitForAccurateLocation(false)
            .setMinUpdateDistanceMeters(10f)
            .setMinUpdateIntervalMillis(500)
            .build()

        // Активация поворота иконки
        compassOrientationProvider = InternalCompassOrientationProvider(this)
        compassOrientationProvider.startOrientationProvider { azimuth, _ ->
            // Обработка азимута, определяющего текущее направление устройства
            updateIconRotation(azimuth)
        }
        startLocationUpdates()
    }



    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.69.194:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendAPI = retrofit.create(BackendAPI::class.java)
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
                    updateUserLocation(location)
            }
        }, null)
    }


    // Вызывается при смене локации пользователем
    private fun updateUserLocation(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        val point = Point(location.latitude, location.longitude)

        if (!userPoints.contains(point))
            userClearedPoints += point
        if (userClearedPoints.size == 1) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val updatePointsReceiveRemote = UpdatePointsReceiveRemote(userPhoneNumber, userClearedPoints)
                    val result = backendAPI.updatePoints(updatePointsReceiveRemote)
                    //userClearedPoints.clear()
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        fogOverlay.addClearedTile(geoPoint)
        Log.d("LOCATION_UPDATE", "Location: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }



    private fun loadUserClearedPoints() {
        val getPointsReceiveRemote = GetPointsReceiveRemote(userPhoneNumber)
        CoroutineScope(Dispatchers.IO).launch {
            val response = backendAPI.getPoints(getPointsReceiveRemote).body()
            if (response != null) {
                userPoints = response.points!!.toMutableList()
                for (point in userPoints)
                {
                    val pointAsGeo = GeoPoint(point.latitude, point.longitude)
                    fogOverlay.addClearedTile(pointAsGeo)
                }
            }
        }
    }


    // КНОПКА. Функция - логика нажатия кнопки возвращения к своей локации
    fun toMyLocationButton(view: View?) {
        runOnUiThread {
            controller.animateTo(myLocationOverlay.myLocation)
            controller.setZoom(10.0)
        }
    }



    // Функция обновления поворота иконки (стрелки пользователя)
    private fun updateIconRotation(inAzimuth: Float) {
        val rotMatrix = Matrix()
        rotMatrix.postRotate(inAzimuth, scaledIcon.width / 2F, scaledIcon.height / 2F)
        val rotatedBitmap = Bitmap.createBitmap(scaledIcon,0,0,scaledIcon.width, scaledIcon.height, rotMatrix,true)
        myLocationOverlay.setPersonIcon(rotatedBitmap)
        myLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        map.invalidate()
    }




    override fun onPause() {
        super.onPause()
        // compassOrientationProvider.stopOrientationProvider() // подозреваю, что из-за этого локация не обновляется при выключенном телефоне
    }

    override fun onResume() {
        super.onResume()
        // compassOrientationProvider.startOrientationProvider{azimuth, _ -> updateIconRotation(azimuth)} // - в onPause() убрал stopOrientation...
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event:ZoomEvent?): Boolean {
        map.invalidate()
        return false
    }

}
