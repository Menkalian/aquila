package de.menkalian.aquila

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val installViewModel: InstallViewModel by viewModels()
    private lateinit var stringUpdateJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        (findViewById<View>(R.id.root).background as Animatable).start()

        installViewModel.viewModelScope.launch {
            if (installViewModel.isApiCompatible()) {
                val updateApk = installViewModel.prepareUpdate()
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(R.string.aquila_update_ready)
                    .setPositiveButton(R.string.aquila_update_confirm) { _, _ ->
                        val installIntent = Intent(Intent.ACTION_VIEW)
                        installIntent.setDataAndType(
                            FileProvider.getUriForFile(
                                application,
                                "${application.applicationContext.packageName}.update.provider",
                                updateApk
                            ),
                            "application/vnd.android.package-archive"
                        )
                        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        application.startActivity(installIntent)
                    }.setNegativeButton(R.string.aquila_update_cancel) { _, _ ->
                        this@MainActivity.finishAffinity()
                    }.show()
            } else {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(R.string.aquila_api_incompatible)
                    .setPositiveButton(R.string.aquila_update_confirm) { _, _ ->
                        this@MainActivity.finishAffinity()
                    }.show()
            }
        }
        stringUpdateJob = installViewModel.viewModelScope.launch {
            val loadingText: TextView = findViewById(R.id.loading_text)
            withContext(Dispatchers.Main) {
                while (true) {
                    loadingText.text = when (Random.nextInt(10)) {
                        0 -> getString(R.string.aquila_loading_string_0)
                        1 -> getString(R.string.aquila_loading_string_1)
                        2 -> getString(R.string.aquila_loading_string_2)
                        3 -> getString(R.string.aquila_loading_string_3)
                        4 -> getString(R.string.aquila_loading_string_4)
                        5 -> getString(R.string.aquila_loading_string_5)
                        6 -> getString(R.string.aquila_loading_string_6)
                        7 -> getString(R.string.aquila_loading_string_7)
                        8 -> getString(R.string.aquila_loading_string_8)
                        else -> getString(R.string.aquila_loading_string_9)
                    }
                    delay(10000)
                }
            }
        }
    }
}