package com.manet.mobile_ad_hoc.connection

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.manet.mobile_ad_hoc.R


class EnableBluetoothFragment : Fragment() {

    private val bluetoothEnableObserver = Observer<Boolean> { shouldPrompt ->
        if (!shouldPrompt) {

            findNavController().navigate(R.id.action_enableBluetoothFragment_to_enableLocationFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Server.requestEnableBluetooth.observe(this, bluetoothEnableObserver)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view : View = inflater.inflate(R.layout.fragment_enable_bluetooth, container, false)

        var btn : Button = view.findViewById(R.id.enableBluetooth)
        btn.setOnClickListener(  View.OnClickListener {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, constants.REQUEST_ENABLE_BT)
        })

        return view;
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            constants.REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    Server.startServer(requireActivity().application)
                }
                super.onActivityResult(requestCode, resultCode, data)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}