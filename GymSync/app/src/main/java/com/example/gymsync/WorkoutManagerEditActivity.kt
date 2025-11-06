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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymsync.adapters.WorkoutExAdapter
import com.example.gymsync.data.local.database.entities.Excercise
import com.example.gymsync.data.local.database.entities.Workout
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WorkoutManagerEditActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var etName: EditText
    private lateinit var etLevel: EditText
    private lateinit var etCount: EditText
    private lateinit var etVideoUrl: EditText
    private lateinit var rvExercises: RecyclerView
    private lateinit var workoutRef: DocumentReference
    private lateinit var workout: Workout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_manager_edit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.workout_edit_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etName = findViewById(R.id.etWorkoutName)
        etLevel = findViewById(R.id.etWorkoutLevel)
        etCount = findViewById(R.id.etWorkoutCount)
        etVideoUrl = findViewById(R.id.etWorkoutVideoUrl)
        rvExercises = findViewById(R.id.rvExercisesInWorkout)

        val workoutPath = intent.getStringExtra("workoutRefPath")
        if (workoutPath == null) {
            Toast.makeText(this, "Referencia del workout no encontrada.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        workoutRef = db.document(workoutPath)

        // Load workout data
        loadWorkoutData()

        val btnModify = findViewById<Button>(R.id.btnModificarEntrenador)
        val btnDelete = findViewById<Button>(R.id.btnEliminarEntrenador)
        val btnBack = findViewById<Button>(R.id.btnVolverEntrenador)

        btnModify.setOnClickListener { updateWorkout() }
        btnDelete.setOnClickListener { deleteWorkoutCascade() }
        btnBack.setOnClickListener {
            startActivity(Intent(this, WorkoutManager::class.java))
            finish()
        }
    }

    private fun loadWorkoutData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val doc = workoutRef.get().await()
                if (!doc.exists()) {
                    Toast.makeText(this@WorkoutManagerEditActivity, "Workout no encontrado.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                workout = Workout(
                    name = doc.getString("name") ?: "",
                    level = doc.getString("level") ?: "",
                    excerciseCount = doc.getString("excerciseCount") ?: "",
                    videourl = doc.getString("videoURL") ?: ""
                )

                etName.setText(workout.name)
                etLevel.setText(workout.level)
                etCount.setText(workout.excerciseCount)
                etVideoUrl.setText(workout.videourl)

                val exercises = getExercisesForWorkout(workoutRef)
                workout.excercises = exercises
                rvExercises.layoutManager = LinearLayoutManager(this@WorkoutManagerEditActivity)
                rvExercises.adapter = WorkoutExAdapter(this@WorkoutManagerEditActivity, exercises)

            } catch (e: Exception) {
                Toast.makeText(this@WorkoutManagerEditActivity, "Error cargando workout.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getExercisesForWorkout(workoutRef: DocumentReference): List<Excercise> {
        val list = mutableListOf<Excercise>()
        try {
            val snapshot = db.collection("Excercises")
                .whereEqualTo("workoutRef", workoutRef)
                .get()
                .await()
            for (doc in snapshot.documents) {
                val exc = Excercise(
                    excerciseName = doc.getString("name") ?: "",
                    excerciseDescription = doc.getString("description") ?: "",
                    breakTime = doc.getString("breakTime") ?: "",
                    workoutRef = workoutRef,
                    nameWorkout = workoutRef.id
                )
                list.add(exc)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al obtener ejercicios.", Toast.LENGTH_SHORT).show()
        }
        return list
    }

    private fun updateWorkout() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val updatedData = mapOf(
                    "name" to etName.text.toString(),
                    "level" to etLevel.text.toString(),
                    "excerciseCount" to etCount.text.toString(),
                    "videoURL" to etVideoUrl.text.toString()
                )
                workoutRef.update(updatedData).await()
                Toast.makeText(this@WorkoutManagerEditActivity, "Workout actualizado correctamente.", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@WorkoutManagerEditActivity, "Error al actualizar workout.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteWorkoutCascade() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val excSnapshot = db.collection("Excercises")
                    .whereEqualTo("workoutRef", workoutRef)
                    .get()
                    .await()

                for (excDoc in excSnapshot.documents) {
                    val excRef = excDoc.reference
                    val seriesSnapshot = db.collection("Series")
                        .whereEqualTo("excerciseRef", excRef)
                        .get()
                        .await()

                    for (seriesDoc in seriesSnapshot.documents) {
                        db.collection("Series").document(seriesDoc.id).delete().await()
                    }
                    db.collection("Excercises").document(excDoc.id).delete().await()
                }

                workoutRef.delete().await()

                Toast.makeText(this@WorkoutManagerEditActivity, "Workout eliminado correctamente.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@WorkoutManagerEditActivity, WorkoutManager::class.java))
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@WorkoutManagerEditActivity, "Error al eliminar workout.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
