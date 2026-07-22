package com.example.atm.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.atm.R
import com.example.atm.objects.History
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val gridView = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.history_item, parent, false)
        val textView = gridView!!.findViewById<TextView>(R.id.txt)
        val dateView = gridView!!.findViewById<TextView>(R.id.txt_date)
        val imgView = gridView!!.findViewById<ImageView>(R.id.img_icon)

        val formatter = DateTimeFormatter.ofPattern("MM-dd-yy-HH-mm-ss")
        val dateTime = LocalDateTime.parse(history[position].dte, formatter)
        val formatter2 = DateTimeFormatter.ofPattern("MMMM d'st,' yyyy h:mma", Locale.ENGLISH)
        val formattedDate = dateTime.format(formatter2)

        textView.text = history[position].txt
        dateView.text = formattedDate
        if (history[position].type == 1) {
            imgView.setImageResource(R.drawable.checkings)
        }
        else {
            imgView.setImageResource(R.drawable.savings)
        }

        return gridView
    }
}