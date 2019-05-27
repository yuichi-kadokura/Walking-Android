package com.ahoworld.walking.viewmodel

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.databinding.ObservableBoolean
import android.graphics.Color
import android.location.Location
import android.view.View
import android.widget.AdapterView
import com.ahoworld.walking.kml.Constants
import com.ahoworld.walking.kml.CourseModel
import com.ahoworld.walking.kml.KmsModel
import com.ahoworld.walking.kml.PlaceModel
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface MapsViewModel {

    interface Inputs {
        fun setMarker(marker: Marker)
        fun onMapReady(googleMap: GoogleMap)
        fun onMapLoaded()
        fun onLocationResult(locationResult: LocationResult?)
        fun setPlacemarkList(list: List<PlaceModel>, position: Int)
        fun setCourseList(list: List<CourseModel>, position: Int)
    }

    interface Outputs {
        fun onCompleteRightDrawer(): Observable<LatLngBounds>
        fun setupRightDrawer(): Observable<Pair<List<PlaceModel>, Int>>
        fun setupRightDrawerMap(): Observable<Pair<LatLng, String>>
        fun setupMarker(): Observable<Marker>
        fun clickDrawer(): Observable<ViewModel.Irrelevant>
        fun clickRightDrawer(): Observable<ViewModel.Irrelevant>
        fun closeDrawer(): Observable<ViewModel.Irrelevant>
        fun renewRightDrawer(): Observable<ViewModel.Irrelevant>
        fun distantLocation(): Observable<ViewModel.Irrelevant>
        fun setupLayer(): Observable<Pair<Int, GoogleMap>>
        fun readPlacemark(): Observable<Int>
        fun readCourse(): Observable<Int>
    }

    class ViewModel : Inputs, Outputs, MapsViewModel {

        enum class Irrelevant {
            INSTANCE
        }
        var starVisibility = ObservableBoolean(false)
        var isStarEnabled = ObservableBoolean(false)
        var noPointVisibility = ObservableBoolean(false)

        private var mSharedPreferences: SharedPreferences
        private lateinit var mMap: GoogleMap
        private lateinit var mCircle: Circle
        private var mSelectedKms: KmsModel? = null
        private var mPlaceList: List<PlaceModel> = mutableListOf()
        private var mMarker: Marker? = null

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val onCompleteRightDrawer = PublishSubject.create<LatLngBounds>()
        private val setupRightDrawer = PublishSubject.create<Pair<List<PlaceModel>, Int>>()
        private val setupRightDrawerMap = PublishSubject.create<Pair<LatLng, String>>()
        private val setupMarker = PublishSubject.create<Marker>()
        private val clickDrawer = PublishSubject.create<Irrelevant>()
        private val clickRightDrawer = PublishSubject.create<Irrelevant>()
        private val closeDrawer = PublishSubject.create<Irrelevant>()
        private val renewRightDrawer = PublishSubject.create<Irrelevant>()
        private val distantLocation = PublishSubject.create<Irrelevant>()
        private val setupLayer = PublishSubject.create<Pair<Int, GoogleMap>>()
        private val readPlacemark = PublishSubject.create<Int>()
        private val readCourse = PublishSubject.create<Int>()

        constructor(sharedPreferences: SharedPreferences) {
            mSharedPreferences = sharedPreferences
        }

        init {
        }

        override fun onCompleteRightDrawer(): PublishSubject<LatLngBounds> = this.onCompleteRightDrawer
        override fun setupRightDrawer(): PublishSubject<Pair<List<PlaceModel>, Int>> = this.setupRightDrawer
        override fun setupRightDrawerMap(): PublishSubject<Pair<LatLng, String>> = this.setupRightDrawerMap
        override fun setupMarker(): PublishSubject<Marker> = this.setupMarker
        override fun clickDrawer(): PublishSubject<Irrelevant> = this.clickDrawer
        override fun clickRightDrawer(): PublishSubject<Irrelevant> = this.clickRightDrawer
        override fun closeDrawer(): PublishSubject<Irrelevant> = this.closeDrawer
        override fun renewRightDrawer(): PublishSubject<Irrelevant> = this.renewRightDrawer
        override fun distantLocation(): PublishSubject<Irrelevant> = this.distantLocation
        override fun setupLayer(): PublishSubject<Pair<Int, GoogleMap>> = this.setupLayer
        override fun readPlacemark(): PublishSubject<Int> = this.readPlacemark
        override fun readCourse(): PublishSubject<Int> = this.readCourse

        //---------------
        // Input Methods
        //---------------
        /**
         * マーカー（GoogleMap上のピン）を設定します。
         * 選択状態のマーカーは最大1つです。
         */
        override fun setMarker(marker: Marker) {
            this.setupMarker.onNext(marker)
            if (mMarker != null) {
                mMarker!!.hideInfoWindow()
            }
            marker.showInfoWindow()
            mMarker = marker
            this.isStarEnabled.set(true)
            setStar(marker)
        }

        /**
         * 左ドロワーのクリック処理をします。
         */
        private fun clickLeftDrawer(position: Int) {
            this.setupLayer.onNext(Pair(position, mMap))
            mSelectedKms = Constants.kmls[position]
            this.readCourse.onNext(position)
        }

        /**
         * GoogleMapの初期化完了イベント処理をします。
         */
        @SuppressLint("MissingPermission")
        override fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap
            mMap.setOnMarkerClickListener { marker ->
                mMap.moveCamera(CameraUpdateFactory.newLatLng(marker!!.position))
                this.inputs.setMarker(marker)
                starVisibility.set(true)
                true
            }
            mMap.setOnCameraMoveListener {
                starVisibility.set(false)
                noPointVisibility.set(false)
            }
            mMap.setOnMapClickListener { _ ->
                starVisibility.set(false)
                noPointVisibility.set(false)
            }
            createCircle()
            // 現在地の表示を有効
            mMap.isMyLocationEnabled = true
            mSelectedKms = Constants.kmls[0]
            this.setupLayer.onNext(Pair(0, mMap))
        }

        /**
         * GoogleMapの読み込み完了イベント処理をします。
         */
        override fun onMapLoaded() {
            this.clickLeftDrawer(0)
            this.clickDrawer.onNext(Irrelevant.INSTANCE)
        }

        /**
         * 位置の移動イベント処理をします。
         */
        override fun onLocationResult(locationResult: LocationResult?) {
            val location = locationResult?.lastLocation ?: return
            val lat = LatLng(location.latitude, location.longitude)
            mCircle.center = lat
        }

        /**
         * ランドマーク一覧を設定します。
         */
        override fun setPlacemarkList(list: List<PlaceModel>, position: Int) {
            mPlaceList = list
            this.setupRightDrawer.onNext(Pair(mPlaceList, position))
        }

        /**
         * コース一覧を設定します。
         * カメラをコースに合わせて移動します。
         */
        override fun setCourseList(list: List<CourseModel>, position: Int) {
            val bounds = LatLngBounds.builder()
            for (course in list) {
                bounds.include(LatLng(course.latitude, course.longitude))
            }
            this.onCompleteRightDrawer.onNext(bounds.build())
            this.readPlacemark.onNext(position)
        }

        //-----------------
        // Private Methods
        //-----------------
        /**
         * 現在地の周りの円を作成します。
         */
        private fun createCircle() {
            val position = LatLng(0.0, 0.0)
            val radiusInMeters = 20.0 // 半径20m
            val strokeColor = Color.parseColor("#FF0000FF") //青
            val shadeColor = Color.parseColor("#440000FF") // 青透明
            val circleOptions =
                CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor)
                    .strokeWidth(8f)
            mCircle = mMap.addCircle(circleOptions)
        }

        /**
         * スターの設定
         */
        private fun setStar(marker: Marker) {
            val flg = mSharedPreferences.getBoolean (mSelectedKms!!.kmlName + "," + marker.position.latitude.toString() + "," + marker.position.longitude.toString(), false)
            this.isStarEnabled.set(flg)
        }

        //------------------------------
        // Click Events from Layout.xml
        //------------------------------
        /**
         * スターのクリックイベント
         */
        fun onStarViewClick(view: View) {
            var results = floatArrayOf(0.0f)
            Location.distanceBetween(
                mCircle.center.latitude,
                mCircle.center.longitude,
                mMarker!!.position.latitude,
                mMarker!!.position.longitude,
                results
            )
            val meter = results[0]
            // 20m以内か判定
            if (meter <= 20.0f) {
                if (!mSharedPreferences.getBoolean(mSelectedKms!!.kmlName + "," + mMarker!!.position.latitude.toString() + "," + mMarker!!.position.longitude.toString(), false)) {
                    val editor = mSharedPreferences.edit()
                    editor.putBoolean(
                        mSelectedKms!!.kmlName + "," + mMarker!!.position.latitude.toString() + "," + mMarker!!.position.longitude.toString(),
                        true
                    )
                    editor.apply()
                    isStarEnabled.set(true)
                }
            } else {
                this.distantLocation().onNext(Irrelevant.INSTANCE)
            }
            this.renewRightDrawer.onNext(Irrelevant.INSTANCE)
        }

        /**
         * 左ドロワーアイコンクリック
         */
        fun onDrawerOpenButtonClick(view: View) {
            this.clickDrawer.onNext(Irrelevant.INSTANCE)
        }

        /**
         * 右ドロワーアイコンクリック
         */
        fun onRightDrawerOpenButtonClick(view: View) {
            this.clickRightDrawer.onNext(Irrelevant.INSTANCE)
        }

        /**
         * 左ドロワー項目クリック
         */
        fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            this.clickLeftDrawer(position)
        }

        /**
         * 右ドロワー項目クリック
         */
        fun onRightItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val lat = LatLng(mPlaceList[position].latitude, mPlaceList[position].longitude)
            val title = mPlaceList[position].name + "／" + mPlaceList[position].english
            this.setupRightDrawerMap.onNext(Pair(lat, title))
            starVisibility.set(true)
        }
    }
}