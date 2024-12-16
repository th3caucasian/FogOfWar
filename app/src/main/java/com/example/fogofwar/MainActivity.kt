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
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.database.columns.marker.MarkerDTO
import com.example.features.updatePoints.UpdatePointsReceiveRemote
import com.example.fogofwar.additions.Point
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.add_marker.AddMarkerReceiveRemote
import com.example.fogofwar.backend.remotes.delete_marker.DeleteMarkerReceiveRemote
import com.example.fogofwar.backend.remotes.get_markers.GetMarkersReceiveRemote
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
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity(), MapListener {
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var fogOverlay: FogOverlay
    private lateinit var mapEventsOverlay: MapEventsOverlay

    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private lateinit var currentIcon: Bitmap
    private lateinit var scaledIcon: Bitmap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var compassOrientationProvider: InternalCompassOrientationProvider
    private lateinit var backendAPI: BackendAPI

    private var userPointsFromDB = mutableListOf<Point>()
    private var userMarkersFromDB = mutableListOf<MarkerDTO>()
    private var newlyClearedPoints = mutableListOf<Point>()
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
        mapView.overlays.add(fogOverlay)
        mapView.overlays.add(myLocationOverlay)
        mapView.overlays.add(mapEventsOverlay)
        loadUserData()
    }


    private fun setupMap(activityBinding: ActivityMainBinding) {
        mapView = activityBinding.osmmap
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.getLocalVisibleRect(Rect())

        mapController = mapView.controller
        mapController.setZoom(18.0)

        mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    addMarker(it, "ЭТОТ МАРКЕР ПОСТАВЛЕН ЗДЕСЬ ${p.latitude}, ${p.longitude}")
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return true
            }
        })

        currentIcon = BitmapFactory.decodeResource(resources, R.drawable.location_arrow_2)         // Если использовать эту переменную - иконка не отображается (видимо дело в размере)
        scaledIcon = Bitmap.createScaledBitmap(currentIcon, 100, 100, true)  // Если использовать эту - то всё отлично работает

    }


    private fun setupLocation() {
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
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

        if (!userPointsFromDB.contains(point))
            newlyClearedPoints += point
        if (newlyClearedPoints.size == 1) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val updatePointsReceiveRemote = UpdatePointsReceiveRemote(userPhoneNumber, newlyClearedPoints)
                    val result = backendAPI.updatePoints(updatePointsReceiveRemote)
                    newlyClearedPoints.clear()
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        fogOverlay.addClearedTile(geoPoint)
        Log.d("LOCATION_UPDATE", "Location: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }



    private fun loadUserData() {
        val getPointsReceiveRemote = GetPointsReceiveRemote(userPhoneNumber)
        val getMarkersReceiveRemote = GetMarkersReceiveRemote(userPhoneNumber)
        CoroutineScope(Dispatchers.IO).launch {
            val pointsResponse = backendAPI.getPoints(getPointsReceiveRemote).body()
            if (pointsResponse != null) {
                userPointsFromDB = pointsResponse.points!!.toMutableList()
                for (point in userPointsFromDB) {
                    val pointAsGeo = GeoPoint(point.latitude, point.longitude)
                    fogOverlay.addClearedTile(pointAsGeo)
                }
            }

            val markersResponse = backendAPI.getMarkers(getMarkersReceiveRemote).body()
            if (markersResponse != null) {
                userMarkersFromDB = markersResponse.markers.toMutableList()
                for (marker in userMarkersFromDB) {
                    val pointAsGeo = GeoPoint(marker.location.latitude, marker.location.longitude)
                    addMarker(pointAsGeo, marker.description)
                }
            }
        }
    }



    // Функция обновления поворота иконки (стрелки пользователя)
    private fun updateIconRotation(inAzimuth: Float) {
        val rotMatrix = Matrix()
        rotMatrix.postRotate(inAzimuth, scaledIcon.width / 2F, scaledIcon.height / 2F)
        val rotatedBitmap = Bitmap.createBitmap(scaledIcon,0,0,scaledIcon.width, scaledIcon.height, rotMatrix,true)
        myLocationOverlay.setPersonIcon(rotatedBitmap)
        myLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        mapView.invalidate()
    }


    private fun addMarker(geoPoint: GeoPoint, description: String?) {
        val marker = Marker(mapView).apply {
            position = geoPoint
            title = description
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        marker.setOnMarkerClickListener { _, _ ->
            val alertDialogBuilder = AlertDialog.Builder(this)
                .setTitle("Что сделать с маркером?")
                .setPositiveButton("Добавить в группу (неактивно") { _, _ ->

                }
                .setNeutralButton("Отмена") {_, _ ->}
                .setNegativeButton("Удалить") {_, _ ->
                    deleteMarker(marker)
                }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
            true
        }

        if (userMarkersFromDB.any{it.description == description}) return

        try {
            CoroutineScope(Dispatchers.IO).launch {
                val geoAsPoint = Point(geoPoint.latitude, geoPoint.longitude)
                val addMarkerReceiveRemote = AddMarkerReceiveRemote(userPhoneNumber, geoAsPoint, marker.title)
                val markerId = backendAPI.addMarkers(addMarkerReceiveRemote).body()!!.markerId
                marker.relatedObject = markerId
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        mapView.overlays.add(marker)
        mapView.invalidate()
    }


    private fun deleteMarker(marker: Marker) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val markerId = marker.relatedObject as Long
                backendAPI.deleteMarkers(DeleteMarkerReceiveRemote(userPhoneNumber, markerId))
                mapView.overlays.remove(marker)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // КНОПКА. Функция - логика нажатия кнопки возвращения к своей локации
    fun toMyLocationButton(view: View?) {
        runOnUiThread {
            mapController.animateTo(myLocationOverlay.myLocation)
            mapController.setZoom(10.0)
        }
    }


    fun toTaganrogButton(view: View?) {
        runOnUiThread {
            mapController.animateTo(GeoPoint(userPointsFromDB[0].latitude, userPointsFromDB[1].longitude))
            mapController.setZoom(18.0)
        }
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
        mapView.invalidate()
        return false
    }

}
