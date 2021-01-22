package de.menkalian.aquila

import android.app.Application
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.menkalian.aquila.client.AquilaClient
import kotlinx.coroutines.launch

class TestViewModel(application: Application) : AndroidViewModel(application) {
    fun checkForVersion() {
        viewModelScope.launch {
            val aquilaClient = AquilaClient("android", BuildConfig.VERSION_NAME, getApplication())
            println(aquilaClient.getApiCompatibility())
            println(aquilaClient.updateState())

            val updateApk = aquilaClient.prepareUpdate()
            val installIntent = Intent(Intent.ACTION_VIEW)
            installIntent.setDataAndType(
                FileProvider.getUriForFile(
                    getApplication(),
                    "${getApplication<Application>().applicationContext.packageName}.update.provider",
                    updateApk
                ),
                "application/vnd.android.package-archive"
            )
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            getApplication<Application>().startActivity(installIntent)
        }
    }
}