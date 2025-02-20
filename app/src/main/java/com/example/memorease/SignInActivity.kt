package com.example.memorease

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memorease.databinding.ActivitySignInBinding
import com.example.memorease.databinding.ActivitySignInUserBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.userButton.setOnClickListener(){
            val intent = Intent(this@SignInActivity, SignInUserActivity::class.java)
            startActivity(intent)
        }

        binding.relativeButton.setOnClickListener(){
            val intent = Intent(this@SignInActivity, SignInRelativeActivity::class.java)
            startActivity(intent)
        }

        binding.signUpButton.setOnClickListener(){
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}