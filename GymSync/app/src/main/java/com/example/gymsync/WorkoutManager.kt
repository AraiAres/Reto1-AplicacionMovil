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
import com.example.gymsync.adapters.WorkoutManagerRecyclerAdapter
import com.example.gymsync.data.local.database.entities.Excercise
import com.example.gymsync.data.local.database.entities.Workout
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WorkoutManager : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WorkoutManagerRecyclerAdapter
    private val workouts: MutableList<Pair<Workout, DocumentReference>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_manager)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.workoutmanager)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.rvWorkouts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter =
            WorkoutManagerRecyclerAdapter(this, workouts.map { it.first }) { selectedWorkout ->
                val ref = workouts.find { it.first.name == selectedWorkout.name }?.second
                if (ref != null) {
                    val intent = Intent(this, WorkoutManagerEditActivity::class.java)
                    intent.putExtra("workoutRefPath", ref.path)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this,
                        "Referencia del workout no encontrada.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        recyclerView.adapter = adapter

        val btnBack = findViewById<Button>(R.id.btnVolverEntrenador)
        val btnAdd = findViewById<Button>(R.id.btnAgregarWorkout)
        val btnFilter = findViewById<Button>(R.id.btnFiltrarEntrenador)
        val etFilter = findViewById<EditText>(R.id.etFiltrarWorkoutEntrenador)

        btnBack.setOnClickListener { finish() }

        btnFilter.setOnClickListener {
            val filterText = etFilter.text.toString()
            if (filterText.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa un nivel para filtrar.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                loadWorkouts(filterText)
            }
        }
        btnAdd.setOnClickListener {
            val intent = Intent(this, WorkoutManagerAddActivity::class.java)
            startActivity(intent)
            finish()
        }
        loadWorkouts()
    }

    private fun loadWorkouts(filterLevel: String? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val allWorkouts = getAllWorkouts()
                val filtered = if (filterLevel == null) allWorkouts
                else allWorkouts.filter { it.first.level.equals(filterLevel, ignoreCase = true) }

                workouts.clear()
                workouts.addAll(filtered)
                adapter.updateWorkouts(filtered.map { it.first }.toMutableList())

                if (filtered.isEmpty()) {
                    Toast.makeText(this@WorkoutManager, "No hay Workouts.", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@WorkoutManager, "Error cargando workouts.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private suspend fun getAllWorkouts(): List<Pair<Workout, DocumentReference>> {
        val workoutList = mutableListOf<Pair<Workout, DocumentReference>>()
        try {
            val snapshot = db.collection("Workouts").get().await()
            for (doc in snapshot.documents) {
                val ref = doc.reference
                val workout = Workout(
                    name = doc.getString("name") ?: "",
                    level = doc.getString("level") ?: "",
                    excerciseCount = doc.getString("excerciseCount") ?: "",
                    videourl = doc.getString("videoURL") ?: ""
                )
                val exercises = getAllExercisesForWorkout(ref)
                workout.excercises = exercises
                workoutList.add(Pair(workout, ref))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al obtener workouts.", Toast.LENGTH_SHORT).show()
        }
        return workoutList
    }

    private suspend fun getAllExercisesForWorkout(workoutRef: DocumentReference): List<Excercise> {
        val list = mutableListOf<Excercise>()
        try {
            val snapshot =
                db.collection("Excercises").whereEqualTo("workoutRef", workoutRef).get().await()
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
}
