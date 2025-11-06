package com.example.gymsync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gymsync.data.local.database.entities.User
import com.example.gymsync.data.local.preference.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class UserActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user)

        SessionManager.init(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.es).setOnClickListener {
            val locale = Locale("es")
            Locale.setDefault(locale)

            val config = resources.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)

            val context = createConfigurationContext(config)
            resources.updateConfiguration(config, resources.displayMetrics)

            val refresh = Intent(this, this::class.java)
            startActivity(refresh)
            finish()
        }
        findViewById<Button>(R.id.en).setOnClickListener {
            val locale = Locale("en")
            Locale.setDefault(locale)

            val config = resources.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)

            val context = createConfigurationContext(config)
            resources.updateConfiguration(config, resources.displayMetrics)

            val refresh = Intent(this, this::class.java)
            startActivity(refresh)
            finish()
        }
        findViewById<EditText>(R.id.etNombreCliente).setText(SessionManager.getUser()?.name.toString())
        findViewById<EditText>(R.id.etApellidoCliente).setText(SessionManager.getUser()?.lastname.toString())
        findViewById<EditText>(R.id.etEmailCliente).setText(SessionManager.getUser()?.email.toString())
        findViewById<EditText>(R.id.etFecNacCliente).setText(SessionManager.getUser()?.birthdate.toString())
        findViewById<EditText>(R.id.etpassword).setText(SessionManager.getUser()?.password.toString())
        if (SessionManager.getUser()?.trainer == true) {
            val trainerButton = findViewById<Button>(R.id.btnVolverCliente)
            trainerButton.setText(R.string.button_gestionworkouts)
            trainerButton.setOnClickListener {
                val intent = Intent(this, WorkoutManager::class.java)
                startActivity(intent)
            }
        } else {
            val clientButton = findViewById<Button>(R.id.btnVolverCliente)
            clientButton.setText(R.string.button_historialworkouts)
            clientButton.setOnClickListener {
                val intent = Intent(this, Workouts::class.java)
                startActivity(intent)
            }
        }
        findViewById<Button>(R.id.btnlogout).setOnClickListener {
            SessionManager.clearSession()
            val intent = Intent(this, GymSyncMain::class.java)
            startActivity(intent)
            finish()
        }
        val themeSwitch: SwitchCompat = findViewById(R.id.switchTema)
        themeSwitch.setOnClickListener {
            if (themeSwitch.isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
        val dataModifyButton: Button = findViewById(R.id.btnmodifydata)
        dataModifyButton.setOnClickListener {
            val userOriginalData = SessionManager.getUser()
            val newName = findViewById<EditText>(R.id.etNombreCliente).text.toString()
            val newLastname = findViewById<EditText>(R.id.etApellidoCliente).text.toString()
            val newEmail = findViewById<EditText>(R.id.etEmailCliente).text.toString()
            val newBirthdate = findViewById<EditText>(R.id.etFecNacCliente).text.toString()
            val newPassword = findViewById<EditText>(R.id.etpassword).text.toString()
            var documentId: String
            db.collection("Clients")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.getString("name").toString().equals(
                                userOriginalData?.name.toString(),
                                true
                            )
                            && document.getString("password")
                                .toString() == userOriginalData?.password.toString()
                        ) {
                            documentId = document.id
                            db.collection("Clients").document(documentId).delete()
                                .addOnSuccessListener {
                                    Log.d(
                                        "Firestore",
                                        "User deleted: ${userOriginalData.toString()}"
                                    )
                                }.addOnFailureListener {
                                    Log.d(
                                        "Firestore",
                                        "Error on user delete: ${userOriginalData.toString()}"
                                    )
                                }

                            val registereduser = User(
                                name = newName,
                                lastname = newLastname,
                                birthdate = newBirthdate,
                                email = newEmail,
                                password = newPassword,
                                trainer = userOriginalData?.trainer == true,
                                level = userOriginalData?.level.toString()
                            )

                            db.collection("Clients")
                                .add(registereduser)
                                .addOnSuccessListener { result ->
                                    Toast.makeText(
                                        this,
                                        "Datos actualizados con exito",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d("User added", "Added user: $registereduser")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Firestore", "Error when inserting user", exception)
                                }
                        }
                    }
                }
        }
    }
}