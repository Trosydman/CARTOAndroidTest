package com.carto.androidtest.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carto.androidtest.databinding.ActivityNewMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
