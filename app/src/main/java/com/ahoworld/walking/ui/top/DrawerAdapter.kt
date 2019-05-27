package com.ahoworld.walking.ui.top

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.ahoworld.walking.R
import com.ahoworld.walking.kml.KmsModel

class DrawerAdapter : ArrayAdapter<KmsModel> {

    private var inflater : LayoutInflater? = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

    constructor(context : Context, resource : Int, list: List<KmsModel>) : super(context, resource, list) {}

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view = convertView

        if (view == null) {
            view = inflater!!.inflate(R.layout.item_drawer, parent, false)
            view!!.tag = DrawerViewHolder(
                view.findViewById(R.id.textView1),
                view.findViewById(R.id.textView2),
                view.findViewById(R.id.textView3)
            )
        }
        val viewHolder = view.tag as DrawerViewHolder
        val item = getItem(position)
        viewHolder.titleView.text = item.title
        viewHolder.englishView.text = item.english
        viewHolder.cityView.text = item.city

        return view
    }
}
