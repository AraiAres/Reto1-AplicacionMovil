package com.example.gymsync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.gymsync.data.local.database.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.example.gymsync.data.local.database.entities.User
import com.example.gymsync.data.local.preference.SessionManager
import kotlinx.coroutines.launch

class LoginTabFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val room = AppDatabase.invoke(requireContext())
        SessionManager.init(requireContext())
        val view = inflater.inflate(R.layout.fragment_login_tab, container, false)
        val loginuser: EditText = view.findViewById(R.id.login_username)
        val loginpassword: EditText = view.findViewById(R.id.login_password)
        val trainerchecked : CheckBox = view.findViewById(R.id.areyoutrainer)
        val rememberMeChecked : CheckBox = view.findViewById(R.id.remembermechk)
        val loginbutton : Button = view.findViewById(R.id.login_button)
        lifecycleScope.launch {
            val rememberedUser : User? = room.userDao().getUser()
            if(rememberedUser != null) {
                loginuser.setText(rememberedUser.name)
                loginpassword.setText(rememberedUser.password)
                trainerchecked.isChecked = rememberedUser.trainer
                rememberMeChecked.isChecked = true
            }
        }
        loginbutton.setOnClickListener {
            db.collection("Clients")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val user = User()
                        if(document.getString("name").toString().equals(loginuser.text.toString(),true) && document.getString("password").toString() == loginpassword.text.toString()) {
                            if(trainerchecked.isChecked ==document.getBoolean("trainer")){
                                user.name = document.getString("name").toString()
                                user.lastname = document.getString("lastname").toString()
                                user.email = document.getString("email").toString()
                                user.birthdate = document.getString("birthdate").toString()
                                user.password = document.getString("password").toString()
                                user.level = document.getString("level").toString()
                                user.trainer = document.getBoolean("trainer") ?: false
                                user.useridfirebase = document.id
                                SessionManager.saveUser(user)
                                if(rememberMeChecked.isChecked){
                                    lifecycleScope.launch {
                                        room.userDao().insertUser(user)
                                    }
                                } else {
                                    lifecycleScope.launch {
                                        room.userDao().deleteAllUsers()
                                    }
                                }
                                Log.d("Session", SessionManager.getUser().toString())
                                val intent = Intent(requireContext(), UserActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error when reading users - $exception", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Error when reading users", exception)
                }



        }
        return view

    }

}