package com.xflix.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xflix.app.data.Genre
import com.xflix.app.data.Kind
import com.xflix.app.data.Profile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HomeFilter(val kind: Kind? = null, val genre: Genre? = null) {
    val isActive: Boolean get() = kind != null || genre != null
}

/** State lintas layar, data lokal */
class XflixViewModel : ViewModel() {

    var account by mutableStateOf<String?>(null)
        private set
    var profile by mutableStateOf<Profile?>(null)
        private set
    var homeReady by mutableStateOf(false)
        private set
    var homeFilter by mutableStateOf(HomeFilter())
    var smartDownloads by mutableStateOf(true)
    var overlayOpen by mutableStateOf(false)

    val myList = mutableStateListOf<String>()
    val reminders = mutableStateListOf<String>()
    val liked = mutableStateListOf("jarak", "rimba", "penjaga_mercusuar", "kota_tanpa_tidur")

    // Kunci: id atau id/sX/eY
    val downloads = mutableStateMapOf(
        "kota_tanpa_tidur/s1/e1" to 1f,
        "kota_tanpa_tidur/s1/e2" to 1f,
        "kota_tanpa_tidur/s1/e3" to 1f,
        "jarak" to 1f,
    )
    private val downloadJobs = mutableMapOf<String, Job>()

    // Simulasi panggilan auth
    suspend fun authenticate(email: String) {
        delay(1100)
        account = email
    }

    fun selectProfile(next: Profile) {
        profile = next
        homeReady = false
        homeFilter = HomeFilter()
        // Simulasi muat katalog
        viewModelScope.launch {
            delay(700)
            homeReady = true
        }
    }

    fun signOut() {
        downloadJobs.values.forEach { it.cancel() }
        downloadJobs.clear()
        account = null
        profile = null
        homeReady = false
        myList.clear()
        reminders.clear()
    }

    fun toggleMyList(id: String) {
        if (!myList.remove(id)) myList.add(0, id)
    }

    fun toggleReminder(id: String) {
        if (!reminders.remove(id)) reminders.add(id)
    }

    fun toggleLike(id: String) {
        if (!liked.remove(id)) liked.add(0, id)
    }

    fun startDownload(id: String) {
        if (downloads.containsKey(id)) return
        downloads[id] = 0f
        downloadJobs[id] = viewModelScope.launch {
            // Simulasi unduhan ±4 detik
            var progress = 0f
            while (progress < 1f) {
                delay(90)
                progress = (progress + 0.022f).coerceAtMost(1f)
                downloads[id] = progress
            }
            downloadJobs.remove(id)
        }
    }

    fun removeDownload(id: String) {
        downloadJobs.remove(id)?.cancel()
        downloads.remove(id)
    }
}
