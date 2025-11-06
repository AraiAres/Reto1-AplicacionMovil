package com.example.gymsync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymsync.adapters.WorkoutRecyclerAdapter
import com.example.gymsync.data.local.database.entities.WorkoutHistory
import com.example.gymsync.data.local.preference.SessionManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class Workouts : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var workoutRecycler: RecyclerView
    private lateinit var workoutAdapter: WorkoutRecyclerAdapter
    private val workoutHistoryList: MutableList<WorkoutHistory> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workouts)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_workouts)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnVolverWorkouts).setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
            finish()
        }



        workoutRecycler = findViewById(R.id.rvHistorial)
        workoutRecycler.layoutManager = LinearLayoutManager(this)
        workoutAdapter = WorkoutRecyclerAdapter(this, workoutHistoryList)
        workoutRecycler.adapter = workoutAdapter

        getUserCompletedWorkouts()

        findViewById<Button>(R.id.btnFiltrarWorkouts).setOnClickListener {
            val textValue = findViewById<EditText>(R.id.etFiltroNivelWorkouts).text.toString()
            val intValueFilter: Int? = textValue.toIntOrNull()
            if (intValueFilter != null) {
                getUserCompletedWorkouts(intValueFilter)
            } else {
                Toast.makeText(this, "Ingrese un número válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserCompletedWorkouts(filterLevel: Int? = null) {
        val userId = SessionManager.getUser()?.useridfirebase
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Clients")
            .document(userId)
            .collection("CompletedSeries")
            .get()
            .addOnSuccessListener { completedDocs ->
                if (completedDocs.isEmpty) {
                    Toast.makeText(this, "Sin historial.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val workoutSeriesMap = mutableMapOf<String, MutableList<Pair<DocumentReference, String?>>>()
                val workoutDataMap = mutableMapOf<String, Map<String, Any>>()

                val total = completedDocs.size()
                var processed = 0

                for (doc in completedDocs) {
                    val seriesRef = doc.get("seriesRef") as? DocumentReference ?: continue
                    val completionDate = doc.getString("completedDate")

                    seriesRef.get().addOnSuccessListener { seriesDoc ->
                        val excRef = seriesDoc.get("excerciseRef") as? DocumentReference ?: return@addOnSuccessListener

                        excRef.get().addOnSuccessListener { excDoc ->
                            val workoutRef = excDoc.get("workoutRef") as? DocumentReference ?: return@addOnSuccessListener
                            val workoutId = workoutRef.id

                            workoutRef.get().addOnSuccessListener { workoutDoc ->
                                val name = workoutDoc.getString("name") ?: "Sin nombre"
                                val levelString = workoutDoc.getString("level") ?: ""
                                val level = levelString.toIntOrNull()
                                val excCount = workoutDoc.getString("excerciseCount") ?: "0"
                                val videoURL = workoutDoc.getString("videoURL") ?: ""

                                if (filterLevel == null || level == filterLevel) {
                                    workoutDataMap[workoutId] = mapOf(
                                        "name" to name,
                                        "level" to levelString,
                                        "excCount" to excCount,
                                        "videoURL" to videoURL
                                    )

                                    workoutSeriesMap
                                        .getOrPut(workoutId) { mutableListOf() }
                                        .add(Pair(seriesRef, completionDate))
                                }

                                processed++
                                if (processed == total) {
                                    calculateWorkoutProgress(workoutDataMap, workoutSeriesMap)
                                }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("DEBUG_FIRESTORE", "Error getting user CompletedSeries", it)
            }
    }


    private fun calculateWorkoutProgress(
        workoutDataMap: Map<String, Map<String, Any>>,
        workoutSeriesMap: Map<String, MutableList<Pair<DocumentReference, String?>>>
    ) {
        val workoutCompletionStats = mutableListOf<WorkoutHistory>()
        var workoutsProcessed = 0
        val totalWorkouts = workoutSeriesMap.size

        for ((workoutId, completedList) in workoutSeriesMap) {
            val workoutData = workoutDataMap[workoutId] ?: continue
            val workoutName = workoutData["name"].toString()
            val workoutLevel = workoutData["level"].toString()
            val videoURL = workoutData["videoURL"].toString()

            db.collection("Excercises")
                .whereEqualTo("workoutRef", db.collection("Workouts").document(workoutId))
                .get()
                .addOnSuccessListener { excDocs ->
                    var totalSeriesCount = 0

                    val seriesTasks = excDocs.map { exc ->
                        db.collection("Series")
                            .whereEqualTo("excerciseRef", exc.reference)
                            .get()
                    }

                    Tasks.whenAllSuccess<Any>(seriesTasks)
                        .addOnSuccessListener { results ->
                            results.forEach { r ->
                                val query = r as com.google.firebase.firestore.QuerySnapshot
                                totalSeriesCount += query.size()
                            }

                            val completedCount = completedList.size
                            val percent = ((completedCount.toFloat() / totalSeriesCount.coerceAtLeast(1)) * 100).toInt()

                            val latestDate = completedList
                                .mapNotNull { it.second }
                                .mapNotNull { dateStr ->
                                    try {
                                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                .maxOrNull()

                            val formattedDate = latestDate?.let {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                            } ?: "—"
                            val seriesRefs = completedList.map { it.first }

                            lifecycleScope.launch {
                                val totalTime = calculateEstimatedWorkoutTime(seriesRefs)
                                val historyItem = WorkoutHistory().apply {
                                    workoutname = workoutName
                                    level = workoutLevel
                                    estimatedtime = "$totalTime Segundos"
                                    date = formattedDate
                                    completedExcercisePercentaje = "$percent %"
                                    videolink = videoURL
                                }

                                workoutCompletionStats.add(historyItem)
                                workoutsProcessed++

                                if (workoutsProcessed == totalWorkouts) {
                                    updateRecyclerData(workoutCompletionStats)
                                }
                            }
                        }
                }
        }
    }


    private suspend fun calculateEstimatedWorkoutTime(
        seriesRefs: List<DocumentReference>
    ): Int {
        var totalDuration = 0
        val exerciseBreakTimes = mutableMapOf<String, Int>()

        for (seriesRef in seriesRefs) {
            try {
                val seriesSnapshot = seriesRef.get().await()
                val estimatedDurationStr = seriesSnapshot.getString("estimatedDuration") ?: "0"
                val estimatedDuration = estimatedDurationStr.toIntOrNull() ?: 0

                val exerciseRef = seriesSnapshot.getDocumentReference("excerciseRef")
                var breakTime = 0

                if (exerciseRef != null) {
                    val exId = exerciseRef.id
                    if (exerciseBreakTimes.containsKey(exId)) {
                        breakTime = exerciseBreakTimes[exId]!!
                    } else {
                        val exSnap = exerciseRef.get().await()
                        val breakStr = exSnap.getString("breakTime") ?: "0"
                        breakTime = breakStr.toIntOrNull() ?: 0
                        exerciseBreakTimes[exId] = breakTime
                    }
                }

                totalDuration += estimatedDuration
                Log.d("WorkoutTime", "Adding $estimatedDuration sec + break=$breakTime")
            } catch (e: Exception) {
                Log.e("WorkoutTime", "Error reading series data: ${e.message}")
            }
        }

        val totalSeries = seriesRefs.size
        if (totalSeries > 1) {
            val avgBreak = if (exerciseBreakTimes.isNotEmpty())
                exerciseBreakTimes.values.average().toInt()
            else 0
            totalDuration += (totalSeries - 1) * avgBreak
        }

        Log.d("WorkoutTime", "Total estimated time: $totalDuration sec")
        return totalDuration
    }

    private fun updateRecyclerData(workoutList: List<WorkoutHistory>) {
        workoutHistoryList.clear()
        workoutHistoryList.addAll(workoutList)
        workoutAdapter.notifyDataSetChanged()
        Log.d("DEBUG_UI", "Recycler updated with ${workoutHistoryList.size} items")
    }
}
