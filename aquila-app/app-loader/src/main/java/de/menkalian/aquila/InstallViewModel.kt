package de.menkalian.aquila

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.menkalian.aquila.client.AquilaClient
import de.menkalian.aquila.client.VersionCompatibility
import java.io.File

class InstallViewModel(application: Application) : AndroidViewModel(application) {
    private val client = AquilaClient("android", BuildConfig.VERSION_NAME, application)

    suspend fun isApiCompatible(): Boolean = client.getApiCompatibility() != VersionCompatibility.INCOMPATIBLE

    suspend fun prepareUpdate(): File = client.prepareUpdate()
}