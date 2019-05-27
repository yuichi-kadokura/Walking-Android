package com.ahoworld.walking.kml

import android.content.res.Resources
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

object KmlPerserOfCourse {

    private var mCourseMapCache = mutableMapOf<Int, MutableList<CourseModel>>()

    /**
     * xmlからコース情報を取得します。
     */
    fun getCourseList(resources: Resources, resource: Int): List<CourseModel> {
        if (mCourseMapCache.containsKey(resource)) {
            return mCourseMapCache.get(resource)!!
        }
        val courseList: MutableList<CourseModel> = mutableListOf()

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val inputStream = resources.openRawResource(resource)
        val document = builder.parse(inputStream)
        val folders = document.getElementsByTagName("Folder")
        val folder = folders.item(0) as Element
        val placemarks = folder.getElementsByTagName("Placemark")
        for (i in 0..placemarks.length - 1) {
            val placemark = placemarks.item(i) as Element
            val lineStrings = placemark.getElementsByTagName("LineString")
            if (lineStrings == null || lineStrings.length == 0) {
                continue
            }
            val lineString = lineStrings.item(0) as Element
            val coordinates = lineString.getElementsByTagName("coordinates")
            val coordinate = coordinates.item(0).textContent
            val coords = coordinate.split("\n")
            for (coord in coords) {
                if (coord.isEmpty()) {
                    continue
                }
                val cs = coord.trim().split(",")
                if (cs.size < 2) {
                    continue
                }
                val latitude = cs[1]
                val longitude = cs[0]
                courseList.add(CourseModel(latitude.toDouble(), longitude.toDouble()))
            }
        }
        mCourseMapCache.put(resource, courseList)
        return courseList
    }

}