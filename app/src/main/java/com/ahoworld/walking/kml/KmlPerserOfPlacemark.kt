package com.ahoworld.walking.kml

import android.content.res.Resources
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

object KmlPerserOfPlacemark {

    private var mPlacemarkMapCache = mutableMapOf<Int, MutableList<PlaceModel>>()

    /**
     * xmlからランドマーク情報を取得します。
     */
    fun getPlaceModelList(resources: Resources, resource: Int): List<PlaceModel> {
        if (mPlacemarkMapCache.containsKey(resource)) {
            return mPlacemarkMapCache.get(resource)!!
        }
        val placeList: MutableList<PlaceModel> = mutableListOf()

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val inputStream = resources.openRawResource(resource)
        val document = builder.parse(inputStream)
//            val root = document.documentElement
        val folders = document.getElementsByTagName("Folder")
        val folder = folders.item(0) as Element
        val placemarks = folder.getElementsByTagName("Placemark")
        for (i in 0..placemarks.length - 1) {
            val placemark = placemarks.item(i) as Element
            val name = placemark.getElementsByTagName("name").item(0).textContent
            val points = placemark.getElementsByTagName("Point")
            if (points == null || points.length == 0) {
                // この時はコースかもよ
                continue
            }
            val point = points.item(0) as Element
            val coordinates = point.getElementsByTagName("coordinates").item(0).textContent

            val names = name.split("／")
            val title: String
            val english: String
            if (names.size == 2) {
                title = names[0]
                english = names[1]
            } else {
                title = name
                english = ""
            }
            val latitude = coordinates.trim().split(",")[1]
            val longitude = coordinates.trim().split(",")[0]
            placeList.add(PlaceModel(latitude.toDouble(), longitude.toDouble(), title, english))

        }
        mPlacemarkMapCache.put(resource, placeList)
        return placeList
    }

}