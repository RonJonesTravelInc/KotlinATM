package com.example.atm.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.atm.R
import com.example.atm.objects.History

class HisotryAdapter (
    private val history: List<History>,
    private val context: Context,
)
    : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private lateinit var txt: TextView

    override fun getCount(): Int {
        return history.size
    }

    override fun getItem(position: Int): Any {
        return history[position]
    }

    override fun getItemId(position: Int): Long {
        return history[position].id.toLong()
    }
    // in below function we are getting individual item of grid view.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val gridView = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.gridview_item, parent, false)
        val textView = gridView!!.findViewById<TextView>(R.id.txt)

        textView.text = history[position].txt + " " + history[position].dte

        return gridView
    }
}