package de.menkalian.aquila

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    val installViewModel: InstallViewModel by viewModels()

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
                    .setMessage("Update Bereit. Bitte bestätige die Installation und erlaube alles.")
                    .setPositiveButton("Ok") { interf, id ->
                        val installIntent = Intent(Intent.ACTION_VIEW)
                        installIntent.setDataAndType(
                            FileProvider.getUriForFile(
                                getApplication(),
                                "${application.applicationContext.packageName}.update.provider",
                                updateApk
                            ),
                            "application/vnd.android.package-archive"
                        )
                        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        application.startActivity(installIntent)
                    }.setNegativeButton("Abbrechen") { interf, id ->
                        this@MainActivity.finishAffinity()
                    }.show()
            } else {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("API is incompatible\n Please download a newer version!")
                    .setPositiveButton("Ok") { interf, id ->
                        this@MainActivity.finishAffinity()
                    }
                    .create().show()
            }
        }
    }
}