package com.example.gymsync.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.gymsync.R
import com.example.gymsync.data.local.database.entities.WorkoutHistory

class WorkoutRecyclerAdapter(
    private val context: Context,
    private val workoutList: List<WorkoutHistory>
) : RecyclerView.Adapter<WorkoutRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvNombreEjercicio)
        val level: TextView = view.findViewById(R.id.tvNivel)
        val estimatedTime: TextView = view.findViewById(R.id.tvTiempoPrevisto)
        val date: TextView = view.findViewById(R.id.tvFecha)
        val completedPercent: TextView = view.findViewById(R.id.tvPorcentaje)
        val videoLink: TextView = view.findViewById(R.id.tvLinkVideo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = workoutList[position]
        holder.name.text = item.workoutname
        holder.level.text = item.level
        holder.estimatedTime.text = item.estimatedtime
        holder.date.text = item.date
        holder.completedPercent.text = item.completedExcercisePercentaje
        holder.videoLink.text = item.videolink

        if (item.videolink.isNotEmpty()) {
            holder.videoLink.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, item.videolink.toUri()).apply {
                    setPackage("com.google.android.youtube")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                }catch (e: Exception){
                    context.startActivity(Intent(Intent.ACTION_VIEW, item.videolink.toUri()))
                }
            }
        }
    }

    override fun getItemCount(): Int = workoutList.size
}
