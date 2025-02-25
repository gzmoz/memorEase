
package com.example.memorease

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memorease.databinding.ActivitySignInBinding
import com.example.memorease.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.userButton.setOnClickListener(){
            val intent = Intent(this@SignUpActivity, SignUpUserActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.relativeButton.setOnClickListener(){
            val intent = Intent(this@SignUpActivity, SignUpRelativeActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}









