package com.example.gymsync.adapters

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymsync.data.local.database.entities.Excercise

class WorkoutExAdapter(
    private val context: Context,
    private var exerciseList: List<Excercise>
) : RecyclerView.Adapter<WorkoutExAdapter.ExViewHolder>() {

    inner class ExViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExViewHolder {
        val textView = TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 18f
            setPadding(16, 12, 16, 12)
            setTextColor(0xFF160C28.toInt())
        }
        return ExViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ExViewHolder, position: Int) {
        val exercise = exerciseList[position]
        holder.textView.text = "• ${exercise.excerciseName}"
    }

    override fun getItemCount(): Int = exerciseList.size

    fun updateExercises(newList: List<Excercise>) {
        exerciseList = newList
        notifyDataSetChanged()
    }
}
