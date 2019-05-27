package com.ahoworld.walking.ui.top

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.ahoworld.walking.R
import com.ahoworld.walking.kml.PlaceModel

class RightDrawerAdapter : ArrayAdapter<PlaceModel> {

    private var inflater : LayoutInflater? = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
    private var mData: SharedPreferences
    private var mKmlName: String
    constructor(context : Context, resource : Int, list: List<PlaceModel>, data: SharedPreferences, kmlName: String) : super(context, resource, list) {
        mData = data
        mKmlName = kmlName
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view = convertView

        if (view == null) {
            view = inflater!!.inflate(R.layout.item_right_drawer, parent, false)
            view!!.tag = RightDrawerViewHolder(
                view.findViewById(R.id.textView1),
                view.findViewById(R.id.textView2),
                view.findViewById(R.id.imageView)
            )
        }
        val viewHolder = view.tag as RightDrawerViewHolder
        val item = getItem(position)
        viewHolder.titleView.text = item.name
        viewHolder.englishView.text = item.english

        val flg = mData.getBoolean (mKmlName + "," + item.latitude.toString() + "," + item.longitude.toString(), false)
        if (flg) {
            viewHolder.starView.setImageResource(android.R.drawable.star_big_on)
        } else {
            viewHolder.starView.setImageResource(android.R.drawable.star_big_off)
        }

        return view
    }
}
