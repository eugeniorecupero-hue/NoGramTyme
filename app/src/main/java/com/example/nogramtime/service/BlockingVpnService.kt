package com.example.nogramtime.service

import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * VpnService implementation that disallows traffic to Instagram by placing it
 * onto the disallowed list. When active, the service establishes a dummy VPN
 * interface and adds the target application to the disallowed list using
 * [android.net.VpnService.Builder.addDisallowedApplication]. According to the
 * Android VPN documentation, apps in the disallowed list use the normal system
 * networking as if no VPN was running【169523580676260†L869-L872】.
 */
class BlockingVpnService : VpnService() {
    private var vpnThread: Thread? = null
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        if (vpnThread != null) return START_STICKY
        vpnThread = Thread {
            try {
                val builder = Builder()
                    .setSession("NoGramTimeVPN")
                    .setMtu(1500)
                    // Add an IP address and route so the VPN interface is considered up. Use
                    // a private address that will not collide with common networks.
                    .addAddress("10.111.222.1", 32)
                    .addRoute("0.0.0.0", 0)
                // Deny Instagram traffic
                try {
                    builder.addDisallowedApplication("com.instagram.android")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                vpnInterface = builder.establish()
                // Keep the service alive until interrupted
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(1000)
                    } catch (ie: InterruptedException) {
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopSelf()
            }
        }
        vpnThread!!.start()
        return START_STICKY
    }

    override fun onDestroy() {
        vpnThread?.interrupt()
        try {
            vpnInterface?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}