package com.manet.wifidirect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Toast
import com.manet.mobile_ad_hoc.MainActivity


var TAG : String = "WiFiDirectBroadcastReceiver"


class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val activity: MainActivity,
    private val handler: android.os.Handler
) : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        TODO("Not yet implemented")
    }

}
