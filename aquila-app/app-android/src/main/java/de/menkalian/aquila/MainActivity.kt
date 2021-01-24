package de.menkalian.aquila

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {
    val viewModel: TestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //viewModel.checkForVersion()
    }

    override fun onResume() {
        super.onResume()
        (findViewById<View>(R.id.root).background as Animatable).start()
    }
}