package com.xflix.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import androidx.core.content.ContextCompat
import java.io.File
import java.util.Locale

/** Video di folder Movies/Xflix */
object LocalMedia {

    const val FOLDER = "Movies/Xflix"

    val permission: String =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_VIDEO else Manifest.permission.READ_EXTERNAL_STORAGE

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun relativePath(title: Title, season: Int, episode: Int): String =
        if (title.kind == Kind.Series) {
            String.format(Locale.ROOT, "%s/s%02de%02d.mp4", title.id, season, episode)
        } else {
            "${title.id}/film.mp4"
        }

    // Memori internal dan SD card
    @Suppress("DEPRECATION")
    private fun roots(context: Context): List<File> =
        if (Build.VERSION.SDK_INT >= 30) {
            context.getSystemService(StorageManager::class.java).storageVolumes.mapNotNull { it.directory }
        } else {
            listOf(Environment.getExternalStorageDirectory())
        }

    fun find(context: Context, title: Title, season: Int, episode: Int): File? {
        val path = "$FOLDER/${relativePath(title, season, episode)}"
        return roots(context).map { File(it, path) }.firstOrNull { it.isFile }
    }
}
