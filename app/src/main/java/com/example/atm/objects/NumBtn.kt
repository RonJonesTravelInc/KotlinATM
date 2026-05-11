package com.example.atm.objects

import android.content.Context
import android.widget.Button

class NumBtn(context: Context, ids: Int) : androidx.appcompat.widget.AppCompatButton(context) {
    var type: String = "Number"
    override fun setId(id: Int) {
        super.setId(id)
    }
    init {
        this.id = ids
        this.text = ids.toString()
        this.type = "Number"
    }
    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
    }
}