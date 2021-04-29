/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.manet.mobile_ad_hoc


import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.system.Os.remove
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothlechat.*
import com.example.bluetoothlechat.scan.DeviceScanViewModel
import com.manet.mobile_ad_hoc.connection.Server
import com.manet.mobile_ad_hoc.connection.exhaustive
import com.manet.mobile_ad_hoc.connection.gone
import com.manet.mobile_ad_hoc.connection.visible
import com.manet.mobile_ad_hoc.databinding.FragmentDeviceScanBinding
import com.manet.mobile_ad_hoc.connection.constants.isServer

private const val TAG = "DeviceScanFragment"
const val GATT_KEY = "gatt_bundle_key"
private var CopyscanResults = mutableMapOf<String, BluetoothDevice>()

class Run {
    companion object {
        fun after(delay: Long, process: () -> Unit) {
            Handler().postDelayed({
                process()
            }, delay)
        }
    }
}
class DeviceScanFragment : Fragment() {
    private val deviceConnectionObserver = Observer<DeviceConnectionState> { state ->
        when(state) {
            is DeviceConnectionState.Connected -> {
                val device = state.device
                Log.d(TAG, "Gatt connection observer: have device $device")
            }
            is DeviceConnectionState.Disconnected -> {
//                showDisconnected()
            }
        }
    }
    private val connectionRequestObserver = Observer<BluetoothDevice> { device ->
        Log.d(TAG, "Connection request observer: have device $device")
//        ChatServer.setCurrentChatConnection(device)
    }
    private val messageObserver = Observer<String> { message ->
        Log.d(TAG, "Have message ${message}")
        binding.textview.text = message.toString()
    }

    private var _binding: FragmentDeviceScanBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding
        get() = _binding!!
    private val viewModel: DeviceScanViewModel by viewModels()

//    private val deviceScanAdapter by lazy {
//        DeviceScanAdapter(onDeviceSelected)
//    }

    private val viewStateObserver = Observer<DeviceScanViewState> { state ->
        when (state) {
            is DeviceScanViewState.ActiveScan -> showLoading()
            is DeviceScanViewState.ScanResults -> showResults(state.scanResults)
            is Error -> state.message?.let { showError(it) }
            is DeviceScanViewState.AdvertisementNotSupported -> showAdvertisingError()
            else -> Log.d(TAG,"NOTHING")
        }.exhaustive
    }

    private val onDeviceSelected: (BluetoothDevice) -> Unit = { device ->
        Server.setCurrentChatConnection(device)
//        // navigate back to chat fragment
//        findNavController().popBackStack()
//        findNavController().navigate(R.id.action_deviceScanFragment_to_chatFrag)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeviceScanBinding.inflate(inflater, container, false)

        binding.button.setOnClickListener(View.OnClickListener {
            val message = binding.edit.text.toString()
            var count : Int = 1;
            if(message.isNotEmpty())
            {
                for ((k, v) in CopyscanResults) {

//                    Server.sendMessage(message)
                    Run.after((500*count).toLong(),{
                        onDeviceSelected(v)
                        Server.sendMessage(message)
                    })
                    count++;
                }
            }

        })

//        binding.deviceList.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = deviceScanAdapter
//        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        requireActivity().setTitle("Pair with new device")
//        requireActivity().setTitle(R.string.chat_title)
//        Server.connectionRequest.observe(viewLifecycleOwner, connectionRequestObserver)
        Server.deviceConnection.observe(viewLifecycleOwner, deviceConnectionObserver)
        Server.messages.observe(viewLifecycleOwner, messageObserver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.viewState.observe(viewLifecycleOwner, viewStateObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //viewModel.becomeClient()
    }

    private fun showLoading() {

        Log.d(TAG, "showLoading")
        binding.scanning.visible()

//        binding.deviceList.gone()
        binding.noDevices.gone()
        binding.error.gone()
        binding.chatConfirmContainer.gone()
    }

    private fun showResults(scanResults: Map<String, BluetoothDevice>) {

        Log.d(TAG,"List of device Found")
        for ((k, v) in scanResults) {
            Log.d(TAG,k+"  "+v.name)
            if(v.name == "realme Narzo 10")
                Log.d(TAG,v.name)

        }
        CopyscanResults = scanResults as MutableMap<String, BluetoothDevice>;
        Log.d(TAG,"SIZE : "+scanResults.size)
        if (scanResults.isNotEmpty()) {

//            binding.deviceList.visible()
//            deviceScanAdapter.updateItems(scanResults.values.toList())

//            for ((k, v) in scanResults) {
//                onDeviceSelected(v)
//            }
            binding.chat.visible()
            binding.scanning.gone()
            binding.noDevices.gone()
            binding.error.gone()
            binding.chatConfirmContainer.gone()
        } else {
            showNoDevices()
        }
    }

    private fun showNoDevices() {

        isServer = true;

        binding.noDevices.visible()
        binding.progressBar.visible()
        Run.after(500,{
            binding.progressBar.gone()
        })

//        binding.deviceList.gone()
        binding.scanning.gone()
        binding.error.gone()
        binding.chatConfirmContainer.gone()

//        viewModel.becomeServer()
    }



    private fun showError(message: String) {
        Log.d(TAG, "showError: ")
        binding.error.visible()
        binding.errorMessage.text = message

        // hide the action button if one is not provided
        binding.errorAction.gone()
        binding.scanning.gone()
        binding.noDevices.gone()
        binding.chatConfirmContainer.gone()
    }

    private fun showAdvertisingError() {
        showError("BLE advertising is not supported on this device")
    }
}