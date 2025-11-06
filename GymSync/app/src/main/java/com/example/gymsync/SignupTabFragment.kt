package com.example.gymsync

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gymsync.data.local.database.entities.User
import com.google.firebase.firestore.FirebaseFirestore

class SignupTabFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup_tab, container, false)
        val signupname: EditText = view.findViewById(R.id.signup_name)
        val signuplastname: EditText = view.findViewById(R.id.signup_lastName)
        val signupbirthdate: EditText = view.findViewById(R.id.signup_dateOfBirth)
        val signupemail: EditText = view.findViewById(R.id.signup_email)
        val signuppassword: EditText = view.findViewById(R.id.signup_password)
        val signuppasswordconfirm: EditText = view.findViewById(R.id.signup_confirm)
        val signupisTrainer: CheckBox = view.findViewById(R.id.areyoutrainer)
        val signupbutton: Button = view.findViewById(R.id.signup_button)
        signupbutton.setOnClickListener {
            if (signuppassword.text.toString() == signuppasswordconfirm.text.toString()) {
                val registereduser = User(
                    name = signupname.text.toString().trim(),
                    lastname = signuplastname.text.toString().trim(),
                    birthdate = signupbirthdate.text.toString().trim(),
                    email = signupemail.text.toString().trim(),
                    password = signuppassword.text.toString().trim(),
                    trainer = signupisTrainer.isChecked,
                    level = "0"
                )
                var exists: Boolean = false
                db.collection("Clients").get().addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.getString("name")
                                .toString() == registereduser.name && document.getString("lastname")
                                .toString() == registereduser.lastname && document.getString("password")
                                .toString() == registereduser.password
                        ){
                            Toast.makeText(
                                requireContext(),
                                "El usuario que quiere crear ya existe",
                                Toast.LENGTH_SHORT
                            ).show()
                        exists = true
                    }
                }
                if (!exists) {
                    db.collection("Clients")
                        .add(registereduser)
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "User added: $registereduser",
                                Toast.LENGTH_SHORT
                            ).show()
                            signupname.setText("")
                            signuplastname.setText("")
                            signupbirthdate.setText("")
                            signupemail.setText("")
                            signuppassword.setText("")
                            signuppasswordconfirm.setText("")
                            signupisTrainer.isChecked = false
                            Log.d("User added", "Added user: $registereduser")
                        }
                        .addOnFailureListener { exception ->
                            // Something went wrong
                            Toast.makeText(
                                requireContext(),
                                "Error when inserting users - $registereduser",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("Firestore", "Error when inserting user", exception)
                        }
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Contraseña y confirmar contraseña deben ser iguales",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    return view
}
}