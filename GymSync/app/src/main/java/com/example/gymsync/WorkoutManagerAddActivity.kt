package com.example.gymsync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gymsync.data.local.database.entities.Workout
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutManagerAddActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_manager_add)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.workout_add)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val newWorkoutName: EditText = findViewById(R.id.etWorkoutName)
        val newWorkoutCount: EditText = findViewById(R.id.etWorkoutCount)
        val newWorkoutLevel: EditText = findViewById(R.id.etWorkoutLevel)
        val newWorkoutVideo: EditText = findViewById(R.id.etWorkoutVideoUrl)
        findViewById<Button>(R.id.btnCrearEntrenador).setOnClickListener {
            if (newWorkoutName.text.isEmpty() || newWorkoutCount.text.isEmpty() || newWorkoutLevel.text.isEmpty() || newWorkoutVideo.text.isEmpty()) {
                Toast.makeText(this, "No se rellenaron todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                val newworkout = Workout()
                newworkout.name = newWorkoutName.text.toString()
                newworkout.level = newWorkoutLevel.text.toString()
                newworkout.excerciseCount = newWorkoutCount.text.toString()
                newworkout.videourl = newWorkoutVideo.text.toString()
                addWorkout(newworkout)
                val intent = Intent(this, WorkoutManager::class.java)
                startActivity(intent)
                finish()
            }
        }

        findViewById<Button>(R.id.btnVolverEntrenador).setOnClickListener {
            val intent = Intent(this, WorkoutManager::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun addWorkout(workout: Workout) {
        db.collection("Workouts").add(workout)
            .addOnSuccessListener {
                Toast.makeText(this, "Workout añadido correctamente.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al añadir workout.", Toast.LENGTH_SHORT).show()
            }
    }
}