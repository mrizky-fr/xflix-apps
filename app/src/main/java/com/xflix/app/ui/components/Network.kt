package com.xflix.app.ui.components

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** Status internet, ikut berubah langsung */
@Composable
fun rememberOnline(): Boolean {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(ConnectivityManager::class.java) }
    var online by remember { mutableStateOf(manager.isOnline()) }
    DisposableEffect(manager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                online = caps.hasInternet()
            }

            override fun onLost(network: Network) {
                online = false
            }
        }
        manager.registerDefaultNetworkCallback(callback)
        onDispose { manager.unregisterNetworkCallback(callback) }
    }
    return online
}

private fun ConnectivityManager.isOnline(): Boolean =
    getNetworkCapabilities(activeNetwork)?.hasInternet() == true

// Wajib tervalidasi, bukan sekadar tersambung
private fun NetworkCapabilities.hasInternet(): Boolean =
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
