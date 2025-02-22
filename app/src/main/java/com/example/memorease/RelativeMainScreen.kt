package com.example.memorease

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.memorease.databinding.ActivityMainBinding
import com.example.memorease.databinding.ActivityRelativeMainScreenBinding

class RelativeMainScreen : AppCompatActivity() {
    private lateinit var binding: ActivityRelativeMainScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRelativeMainScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        replaceFragment(RelativeUploadReviewMemoryFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.memories_icon -> replaceFragment(RelativeUploadReviewMemoryFragment())
                R.id.report_icon -> replaceFragment(ReportFragment())
                else ->{

                }
            }
            true
        }

    }

    //method that will replace the fragments
    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()

    }
}