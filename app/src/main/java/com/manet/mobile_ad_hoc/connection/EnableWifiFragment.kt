package com.manet.mobile_ad_hoc.connection

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.manet.mobile_ad_hoc.R


class EnableWifiFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var view : View = inflater.inflate(R.layout.fragment_enable_wifi, container, false)
//        val appContext: Context = container!!.context
//        val wifiManager = appContext.getSystemService(appContext.WIFI_SERVICE )  as WifiManager
//        wifiManager.SetWifiEnabled(state)
            findNavController().navigate(R.id.action_enableWifiFragment_to_deviceScanFragment)
        var btn : Button = view.findViewById(R.id.wifipermission)
        btn.setOnClickListener(View.OnClickListener {
            findNavController().navigate(R.id.action_enableWifiFragment_to_deviceScanFragment)
        })
        return view
    }


}