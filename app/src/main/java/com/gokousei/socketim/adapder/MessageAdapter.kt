package com.gokousei.socketim.adapder

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gokousei.socketim.R

class MessageAdapter(
    private var data: MutableList<String>,
    private var position: MutableList<Float>,
    private val context: Context
) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.message_item, p0, false))
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.message.text = data[p1]
        val cs = ConstraintSet()
        cs.clone(p0.cl)
        cs.setHorizontalBias(p0.message.id, position[p1])
        cs.applyTo(p0.cl)
    }

    fun add(s: String, p: Float) {
        val start = data.count()
        data.add(s)
        position.add(p)
        notifyItemRangeInserted(start, data.count())
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cl: ConstraintLayout = view.findViewById(R.id.message_item)
        val message: TextView = view.findViewById(R.id.message_info)
    }
}