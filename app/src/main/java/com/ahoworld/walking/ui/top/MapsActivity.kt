package com.ahoworld.walking.ui.top

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.Toast
import com.ahoworld.walking.R
import com.ahoworld.walking.databinding.ActivityMapsBinding
import com.ahoworld.walking.kml.Constants
import com.ahoworld.walking.kml.KmlPerserOfCourse
import com.ahoworld.walking.kml.KmlPerserOfPlacemark
import com.ahoworld.walking.viewmodel.MapsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.data.kml.KmlLayer
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity() {

    private lateinit var mMap: GoogleMap
    private var mLayer: KmlLayer? = null
    private lateinit var viewModel: MapsViewModel.ViewModel
    private lateinit var fusedLocationClient : FusedLocationProviderClient

    //------------------
    // Override Methods
    //------------------
    /**
     * onCreate
     */
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // DataBinding
        val binding = DataBindingUtil.setContentView<ActivityMapsBinding>(this, R.layout.activity_maps)
        viewModel = MapsViewModel.ViewModel(getPreferences(Context.MODE_PRIVATE))
        binding.viewModel = viewModel

        viewModel.outputs.clickDrawer()
            .subscribe {
                drawer_layout.openDrawer(Gravity.LEFT)
            }
        viewModel.outputs.clickRightDrawer()
            .subscribe() {
                drawer_layout.openDrawer(Gravity.RIGHT)
            }
        viewModel.outputs.onCompleteRightDrawer()
            .subscribe { bounds ->
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15))
                drawer_layout.closeDrawers()
            }
        viewModel.outputs.setupRightDrawer()
            .subscribe { (placeList, position) ->
                val data = getSharedPreferences("star", Context.MODE_PRIVATE)
                right_drawer.adapter = RightDrawerAdapter(this, R.layout.item_right_drawer, placeList, data, Constants.kmls[position].kmlName)
            }
        viewModel.outputs.setupRightDrawerMap()
            .subscribe { (lat, title) ->
                mMap.moveCamera(CameraUpdateFactory.newLatLng(lat))
                val marker = mMap.addMarker(MarkerOptions()
                    .position(lat)
                    .title(title))
                viewModel.inputs.setMarker(marker)
                drawer_layout.closeDrawers()
            }
        viewModel.outputs.setupMarker()
            .subscribe { marker -> }
        viewModel.outputs.closeDrawer()
            .subscribe {
                drawer_layout.closeDrawers()
            }
        viewModel.outputs.renewRightDrawer()
            .subscribe() {
                (right_drawer.adapter as RightDrawerAdapter).notifyDataSetChanged()
            }
        viewModel.outputs.distantLocation()
            .subscribe {
                Toast.makeText(this, resources.getText(R.string.text_too_far), Toast.LENGTH_SHORT).show()
            }
        viewModel.outputs.setupLayer()
            .subscribe() { (selected, map) ->
                if (mLayer != null) {
                    mLayer!!.removeLayerFromMap()
                }
                mLayer = KmlLayer(mMap, Constants.kmls[selected].kml, this)
                mLayer!!.addLayerToMap()
            }
        viewModel.outputs.readPlacemark()
            .subscribe() { position ->
                this.readPlacemarkList(position)
            }
        viewModel.outputs.readCourse()
            .subscribe() { position ->
                this.readCourseList(position)
            }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync{
            mMap = it
            mMap.setOnMapLoadedCallback {
                viewModel.inputs.onMapLoaded()
            }
            this.viewModel.inputs.onMapReady(it)
        }
        left_drawer.adapter = DrawerAdapter(this, R.layout.item_drawer, Constants.kmls)
        setupLocationProvider()
        this.readPlacemarkList(0)
    }

    //-----------------
    // Private Methods
    //-----------------
    /**
     * xmlからランドマーク一覧を読み込みます。
     */
    private fun readPlacemarkList(position: Int) {
        var placeList = KmlPerserOfPlacemark.getPlaceModelList(resources, Constants.kmls[position].kml)
        viewModel.inputs.setPlacemarkList(placeList, position)
    }

    /**
     * xmlからコース一覧を読み込みます。
     */
    private fun readCourseList(position: Int) {
        val courseList = KmlPerserOfCourse.getCourseList(resources, Constants.kmls[position].kml)
        viewModel.inputs.setCourseList(courseList, position)
    }

    /**
     * 位置情報の初期設定をします。
     */
    @SuppressLint("MissingPermission")
    private fun setupLocationProvider() {
        fusedLocationClient = FusedLocationProviderClient(this)
        val locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                viewModel.inputs.onLocationResult(locationResult)
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }
}
