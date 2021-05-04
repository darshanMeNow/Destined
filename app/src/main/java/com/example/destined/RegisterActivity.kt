package com.example.destined

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.edit_text_email
import kotlinx.android.synthetic.main.activity_register.edit_text_password
import kotlinx.android.synthetic.main.activity_register.progressbar

class RegisterActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth= FirebaseAuth.getInstance()

        button_register.setOnClickListener {
            val email=edit_text_email.text.toString().trim()
            val password=edit_text_password.text.toString().trim()
            //email cheking
            if(email.isEmpty()){
                edit_text_email.error="Email Required"
                edit_text_email.requestFocus()
                return@setOnClickListener
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                edit_text_email.error="Valid Email Required"
                edit_text_email.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty() || password.length < 6) {
                edit_text_password.error = "6 char password required"
                edit_text_password.requestFocus()
                return@setOnClickListener
            }
            registerUser(email, password)

        }


        //go to login page
        text_view_login.setOnClickListener{
            startActivity(Intent (this@RegisterActivity,LoginActivity::class.java))
        }
    }

    private fun registerUser(email: String, password: String) {
        progressbar.visibility= View.VISIBLE

        //TODO("Not yet implemented")
        mAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){ task ->
                progressbar.visibility= View.GONE
                if(task.isSuccessful){
                    //Registration success
                    val intent=Intent(this,HomeActivity::class.java).apply {
                        flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
                else{
                    task.exception?.message?.let {
                        toast(it)
                    }
                }
            }
    }
    override fun onStart() {
        super.onStart()
        mAuth.currentUser?.let {
            login()
        }
    }
}