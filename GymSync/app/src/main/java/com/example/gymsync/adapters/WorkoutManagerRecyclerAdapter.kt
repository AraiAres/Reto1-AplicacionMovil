package com.example.gymsync.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymsync.R
import com.example.gymsync.data.local.database.entities.Workout

class WorkoutManagerRecyclerAdapter(
    private val context: Context,
    private var workoutList: List<Workout>,
    private val onItemClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutManagerRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWorkoutName: TextView = itemView.findViewById(R.id.tvNombreEjercicio)
        val tvWorkoutLevel: TextView = itemView.findViewById(R.id.tvNivelEjercicio)
        val tvExerciseCount: TextView = itemView.findViewById(R.id.tvDescripcionEjercicio)
        val tvVideoURL: TextView = itemView.findViewById(R.id.tvLinkVideo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_workouts, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = workoutList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workoutList[position]
        holder.tvWorkoutName.text = workout.name
        holder.tvWorkoutLevel.text = workout.level
        holder.tvExerciseCount.text = workout.excerciseCount
        holder.tvVideoURL.text = workout.videourl

        holder.itemView.setOnClickListener {
            onItemClick(workout)
        }
    }

    fun updateWorkouts(newList: MutableList<Workout>) {
        workoutList = newList
        notifyDataSetChanged()
    }
}
