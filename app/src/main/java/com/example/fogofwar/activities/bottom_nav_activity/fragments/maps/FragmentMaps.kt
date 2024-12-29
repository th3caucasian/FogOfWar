package com.example.fogofwar.activities.bottom_nav_activity.fragments.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.database.columns.marker.MarkerDTO
import com.example.features.updatePoints.UpdatePointsReceiveRemote
import com.example.fogofwar.R
import com.example.fogofwar.activities.marker_groups_activity.MarkerGroupsActivity
import com.example.fogofwar.additions.Point
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.add_marker.AddMarkerReceiveRemote
import com.example.fogofwar.backend.remotes.delete_marker.DeleteMarkerReceiveRemote
import com.example.fogofwar.backend.remotes.get_groups_of_marker.GetGroupsOfMarkerReceiveRemote
import com.example.fogofwar.backend.remotes.get_markers.GetMarkersReceiveRemote
import com.example.fogofwar.backend.remotes.get_points.GetPointsReceiveRemote
import com.example.fogofwar.databinding.AlertDialogMarkerBinding
import com.example.fogofwar.databinding.FragmentMapsBinding
import com.example.fogofwar.overlays.FogOverlay
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class FragmentMaps : Fragment(), MapListener {
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var fogOverlay: FogOverlay
    private lateinit var mapEventsOverlay: MapEventsOverlay

    private lateinit var binding: FragmentMapsBinding
    private lateinit var mapView: MapView
    private lateinit var mapViewMarkers: MapView
    private lateinit var mapPointsController: IMapController
    private lateinit var mapMarkersController: IMapController
    private lateinit var buttonMyLocation: Button
    private lateinit var buttonToTaganrog: Button
    private lateinit var buttonChangeMap: Button

    private lateinit var currentIcon: Bitmap
    private lateinit var scaledIcon: Bitmap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var compassOrientationProvider: InternalCompassOrientationProvider
    private lateinit var backendAPI: BackendAPI
    private lateinit var userPhoneNumber: String

    private var userPointsFromDB = mutableListOf<Point>()
    private var userMarkersFromDB = mutableListOf<MarkerDTO>()
    private var newlyClearedPoints = mutableListOf<Point>()
    private var activeMapFog = true
    private val REQUEST_CODE = 100



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        val root = binding.root
        Configuration.getInstance().userAgentValue = "FogOfWar/1.0"
        setupRetrofit()
        loadUserData()

        setupButtons(binding)
        setupMap(binding)
        setupMapMarkers(binding)
        setupLocationOverlay()


        fogOverlay = FogOverlay()
        mapView.overlays.add(fogOverlay)
        mapViewMarkers.overlays.add(mapEventsOverlay)
        mapView.overlays.add(myLocationOverlay)
        mapView.visibility = View.VISIBLE
        mapViewMarkers.visibility = View.INVISIBLE
        return root
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupMap(fragmentMapsBinding: FragmentMapsBinding) {
        mapView = fragmentMapsBinding.osmmap
        val tileProvider = MapTileProviderBasic(requireActivity().applicationContext, TileSourceFactory.MAPNIK)
        val tileRequestCompleteHandler = SimpleInvalidationHandler(mapView)
        tileProvider.setTileRequestCompleteHandler(tileRequestCompleteHandler)
        mapView.tileProvider = tileProvider
        mapView.setMultiTouchControls(true)
        mapView.getLocalVisibleRect(Rect())
        mapView.setOnTouchListener {v, _ ->
            v.parent?.requestDisallowInterceptTouchEvent(true)
            false
        }
        mapView.maxZoomLevel = 20.0
        mapView.minZoomLevel = 5.0

        mapPointsController = mapView.controller
        mapPointsController.setZoom(18.0)

        currentIcon = BitmapFactory.decodeResource(resources, R.drawable.location_arrow_2)         // Если использовать эту переменную - иконка не отображается (видимо дело в размере)
        scaledIcon = Bitmap.createScaledBitmap(currentIcon, 100, 100, true)  // Если использовать эту - то всё отлично работает
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupMapMarkers(fragmentMapsBinding: FragmentMapsBinding) {
        mapViewMarkers = fragmentMapsBinding.markerMap
        val tileProvider = MapTileProviderBasic(requireActivity().applicationContext, TileSourceFactory.MAPNIK)
        val tileRequestCompleteHandler = SimpleInvalidationHandler(mapView)
        tileProvider.setTileRequestCompleteHandler(tileRequestCompleteHandler)
        mapViewMarkers.tileProvider = tileProvider
        mapViewMarkers.setMultiTouchControls(true)
        mapViewMarkers.getLocalVisibleRect(Rect())
        mapViewMarkers.setOnTouchListener {v, _ ->
            v.parent?.requestDisallowInterceptTouchEvent(true)
            false
        }
        mapViewMarkers.maxZoomLevel = 20.0
        mapViewMarkers.minZoomLevel = 5.0


        mapMarkersController = mapViewMarkers.controller
        mapMarkersController.setZoom(18.0)

        mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    addMarker(it, "ЭТОТ МАРКЕР ПОСТАВЛЕН ЗДЕСЬ ${p.latitude}, ${p.longitude}", null)
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


    private fun setupLocationOverlay() {
        // TODO: ИЗ-ЗА ЭТИХ СТРОЧЕК СКОРЕЕ ВСЕГО ЛОКАЦИЯ НЕПРАВИЛЬНО ОТОБРАЖАЕТСЯ
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.isDrawAccuracyEnabled = false
        myLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        myLocationOverlay.setPersonIcon(scaledIcon)

        // Активация локации (отслеживание перемещения)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setWaitForAccurateLocation(false)
            .setMinUpdateDistanceMeters(10f)
            .setMinUpdateIntervalMillis(500)
            .build()

        // Активация поворота иконки
        compassOrientationProvider = InternalCompassOrientationProvider(requireContext())
        compassOrientationProvider.startOrientationProvider { azimuth, _ ->
            // Обработка азимута, определяющего текущее направление устройства
            updateIconRotation(azimuth)
        }
        startLocationUpdates()
    }


    // Функция обновления поворота иконки (стрелки пользователя)
    private fun updateIconRotation(inAzimuth: Float) {
        val rotMatrix = Matrix()
        rotMatrix.postRotate(inAzimuth, scaledIcon.width / 2F, scaledIcon.height / 2F)
        val rotatedBitmap = Bitmap.createBitmap(scaledIcon,0,0,scaledIcon.width, scaledIcon.height, rotMatrix,true)
        myLocationOverlay.setPersonIcon(rotatedBitmap)
        myLocationOverlay.setPersonAnchor(0.5F, 0.5F)
        when (activeMapFog) {
            true -> mapView.invalidate()
            false -> mapViewMarkers.invalidate()
        }
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Доделать
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations)
                    updateUserPoints(location)
            }
        }, null)
    }


    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://45.91.8.232:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendAPI = retrofit.create(BackendAPI::class.java)
    }


    private fun setupButtons(fragmentMapsBinding: FragmentMapsBinding) {
        buttonMyLocation = fragmentMapsBinding.buttonMyLocation
        buttonToTaganrog = fragmentMapsBinding.buttonToTaganrog
        buttonChangeMap = fragmentMapsBinding.button
        buttonMyLocation.setOnClickListener {
            when (activeMapFog){
                true -> {
                    mapPointsController.animateTo(myLocationOverlay.myLocation)
                    mapPointsController.setZoom(16.0)
                }
                false -> {
                    mapMarkersController.animateTo(myLocationOverlay.myLocation)
                    mapMarkersController.setZoom(16.0)
                }
            }

        }

        buttonToTaganrog.setOnClickListener {
            when (activeMapFog){
                true -> {
                    mapPointsController.animateTo(GeoPoint(47.207451,38.9398434))   // Taganrog
                    mapPointsController.setZoom(18.0)
                }
                false -> {
                    mapMarkersController.animateTo(GeoPoint(47.207451,38.9398434))  // Taganrog
                    mapMarkersController.setZoom(18.0)
                }
            }

        }

        buttonChangeMap.setOnClickListener {
            when (activeMapFog) {
                true -> {
                        mapView.visibility = View.INVISIBLE
                        mapViewMarkers.visibility = View.VISIBLE
                        mapViewMarkers.onResume()
                        mapView.onPause()
                        activeMapFog = !activeMapFog
                        mapPointsController.animateTo(GeoPoint(47.207451,38.9398434)) // Taganrog
                }
                false -> {
                    mapViewMarkers.visibility = View.INVISIBLE
                    mapView.visibility = View.VISIBLE
                    mapView.onResume()
                    mapViewMarkers.onPause()
                    activeMapFog = !activeMapFog
                    mapMarkersController.animateTo(GeoPoint(47.207451,38.9398434))  // Taganrog
                }

            }

        }

    }


    // Вызывается при смене локации пользователем
    private fun updateUserPoints(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        val point = Point(location.latitude, location.longitude)

        if (!userPointsFromDB.contains(point))
            newlyClearedPoints += point
        if (newlyClearedPoints.size == 1) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val updatePointsReceiveRemote = UpdatePointsReceiveRemote(userPhoneNumber, newlyClearedPoints)
                    backendAPI.updatePoints(updatePointsReceiveRemote)
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
        userPhoneNumber = arguments?.getString("user_phone_number", "null")!!
        if (userPhoneNumber == "null") {
            val sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", MODE_PRIVATE)
            userPhoneNumber = sharedPreferences.getString("user_phone_number", "null")!!
        }


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
                    addMarker(pointAsGeo, marker.description, marker.id)
                }
            }
        }
    }


    private fun addMarker(geoPoint: GeoPoint, description: String?, markerId: Long?) {
        val marker = Marker(mapViewMarkers).apply {
            position = geoPoint
            title = description
            relatedObject = markerId
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }


        if (!userMarkersFromDB.any{it.description == description}) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val geoAsPoint = Point(geoPoint.latitude, geoPoint.longitude)
                    val addMarkerReceiveRemote = AddMarkerReceiveRemote(userPhoneNumber, geoAsPoint, marker.title)
                    val _markerId = backendAPI.addMarker(addMarkerReceiveRemote).body()!!.markerId
                    marker.relatedObject = _markerId
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }




        marker.setOnMarkerClickListener { _, _ ->
            val dialogView = layoutInflater.inflate(R.layout.alert_dialog_marker, null)
            val groupNameView = dialogView.findViewById<TextView>(R.id.groupName)
            val descriptionView = dialogView.findViewById<TextView>(R.id.description)
            val buttonMarkerToGroup = dialogView.findViewById<Button>(R.id.buttonMarkerToGroup)
            val buttonDeleteMarker = dialogView.findViewById<Button>(R.id.buttonDeleteMarker)
            val buttonDeleteMarkerFromGroup = dialogView.findViewById<Button>(R.id.buttonDeleteMarkerFromGroup)
            var groupNames: List<String>



            val alertDialogBuilder = AlertDialog.Builder(requireContext())
                .setView(dialogView)
            val alertDialog = alertDialogBuilder.create()
            var text: String

            CoroutineScope(Dispatchers.IO).launch {
                groupNames = backendAPI.getGroupsOfMarker(GetGroupsOfMarkerReceiveRemote(marker.relatedObject as Long)).body()!!.markerGroups
                withContext(Dispatchers.Main) {
                    if (groupNames.isEmpty()) {
                        text = "Без группы"
                        groupNameView.text = text
                        buttonDeleteMarkerFromGroup.isClickable = false
                        buttonDeleteMarkerFromGroup.backgroundTintList = ContextCompat.getColorStateList(requireActivity(), R.color.grey)
                    }
                    else {
                        if (groupNames.size > 1)
                            text = "${groupNames[0]} и ещё ${groupNames.size}"
                        else
                            text = groupNames[0]
                        groupNameView.text = text
                        buttonDeleteMarkerFromGroup.isClickable = true
                        buttonDeleteMarkerFromGroup.backgroundTintList = ContextCompat.getColorStateList(requireActivity(), R.color.red)
                    }

                }
            }

            descriptionView.text = marker.title

            buttonMarkerToGroup.setOnClickListener {
                val intent = Intent(requireContext(), MarkerGroupsActivity::class.java)
                intent.putExtra("action", "add")
                intent.putExtra("marker_id", marker.relatedObject as Long)
                intent.putExtra("user_phone_number", userPhoneNumber)
                startActivity(intent)
            }

            buttonDeleteMarker.setOnClickListener {
                deleteMarker(marker)
                alertDialog.cancel()
            }

            buttonDeleteMarkerFromGroup.setOnClickListener {
                val intent = Intent(requireContext(), MarkerGroupsActivity::class.java)
                intent.putExtra("action", "delete")
                intent.putExtra("marker_id", marker.relatedObject as Long)
                intent.putExtra("user_phone_number", userPhoneNumber)
                startActivity(intent)
            }


            alertDialog.show()
            true
        }


        mapViewMarkers.overlays.add(marker)
        mapViewMarkers.invalidate()
    }


    private fun deleteMarker(marker: Marker) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val markerId = marker.relatedObject as Long
                mapViewMarkers.overlays.remove(marker)
                backendAPI.deleteMarkers(DeleteMarkerReceiveRemote(userPhoneNumber, markerId))
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        mapView.invalidate()
        return false
    }

}