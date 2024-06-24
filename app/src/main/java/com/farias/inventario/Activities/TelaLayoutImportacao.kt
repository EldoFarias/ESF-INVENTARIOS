package com.farias.inventario.Activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.farias.inventario.databinding.ActivityTelaLayoutImportacaoBinding

class TelaLayoutImportacao : AppCompatActivity() {

    private val binding by lazy {
        ActivityTelaLayoutImportacaoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }

}
