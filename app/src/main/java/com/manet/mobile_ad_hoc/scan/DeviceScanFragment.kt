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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothlechat.*
import com.example.bluetoothlechat.chat.MessageAdapter
import com.example.bluetoothlechat.scan.DeviceScanViewModel
import com.manet.mobile_ad_hoc.connection.Server
import com.manet.mobile_ad_hoc.connection.constants.globalSuccess
import com.manet.mobile_ad_hoc.connection.constants.isServer
import com.manet.mobile_ad_hoc.connection.exhaustive
import com.manet.mobile_ad_hoc.connection.gone
import com.manet.mobile_ad_hoc.connection.visible
import com.manet.mobile_ad_hoc.databinding.FragmentDeviceScanBinding
import com.manet.mobile_ad_hoc.scan.Message
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

private const val TAG = "DeviceScanFragment"
const val GATT_KEY = "gatt_bundle_key"
private var CopyscanResults = mutableMapOf<String, BluetoothDevice>()
private var UniversalResults = mutableMapOf<String, BluetoothDevice>()

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
//                chatWith(device)
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
    private val messageObserver = Observer<Message> { message ->
        Log.d(TAG, "Have message ${message}")
//        binding.textview.text = message.toString()
        adapter.addMessage(message)
    }


    private val adapter = MessageAdapter()
    var t: Timer = Timer()

    private var _binding: FragmentDeviceScanBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding
        get() = _binding!!
    private val viewModel: DeviceScanViewModel by viewModels()

//    private val deviceScanAdapter by lazy {
//        DeviceScanAdapter(onDeviceSelected)
//    }
private val _repeatSearch = MutableLiveData<Int>()
    val repeatSearch = _repeatSearch as LiveData<Int>

    private val viewStateObserver = Observer<DeviceScanViewState> { state ->
        when (state) {
            is DeviceScanViewState.ActiveScan -> showLoading()
            is DeviceScanViewState.ScanResults -> showResults(state.scanResults)
            is Error -> state.message?.let { showError(it) }
            is DeviceScanViewState.AdvertisementNotSupported -> showAdvertisingError()
            else -> Log.d(TAG, "NOTHING")
        }.exhaustive
    }

//    private val onDeviceSelected: (BluetoothDevice) -> Unit = { device ->
//        Server.setCurrentChatConnection(device)
////        // navigate back to chat fragment
////        findNavController().popBackStack()
////        findNavController().navigate(R.id.action_deviceScanFragment_to_chatFrag)
//
//    }
    fun onDeviceSelected(device : BluetoothDevice,msg : String)
    {
        Server.setCurrentChatConnection(device,msg)
    }
    val UPDATE_INTERVAL = 8000L
    private val updateWidgetHandler = Handler()
    private var updateWidgetRunnable: Runnable = Runnable {
        run {
            //Update UI

            Log.d("TAG","start1")
            viewModel.startScanAgain()
            Log.d("TAG","start2")
            // Re-run it after the update interval
            updateWidgetHandler.postDelayed(updateWidgetRunnable, UPDATE_INTERVAL)
        }

    }

    override fun onResume() {
        super.onResume()
        updateWidgetHandler.postDelayed(updateWidgetRunnable, UPDATE_INTERVAL)
    }

    override fun onPause() {
        super.onPause()
        updateWidgetHandler.removeCallbacks(updateWidgetRunnable);
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeviceScanBinding.inflate(inflater, container, false)

        binding.messages.layoutManager = LinearLayoutManager(context)
        binding.messages.adapter = adapter

        binding.sendMessage.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "SENDMESSGE PRESSED")
            val message = binding.messageText.text.toString()
            var count: Int = 1;
            var tempFlag = false
            if (message.isNotEmpty()) {
                Log.d(TAG, "SENDMESSGE PRESSED")

                for ((k, v) in CopyscanResults) {
                    Run.after((500 * count).toLong(), {
                        onDeviceSelected(v,message)
//                        Server.sendMessage(message)
                    })
                    count++;
                }

                globalSuccess = false;
                Log.d("TAG","COUNT : "+count)
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

        requireActivity().setTitle("Connecting...")
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
        if (isServer)
            requireActivity().setTitle("Server")
        else
            requireActivity().setTitle("Client")
        Log.d(TAG, "List of device Found")
        for ((k, v) in scanResults) {
//            onDeviceSelected(v)
            if(!UniversalResults.containsKey(k))
            {
                UniversalResults[k] = v
                onDeviceSelected(v,"")
            }
            Log.d(TAG, k + "  " + v.name)

        }
        CopyscanResults = scanResults as MutableMap<String, BluetoothDevice>;
        Log.d(TAG, "SIZE : " + scanResults.size)
        if (scanResults.isNotEmpty()) {

//            binding.deviceList.visible()
//            deviceScanAdapter.updateItems(scanResults.values.toList())

//            for ((k, v) in scanResults) {
//                onDeviceSelected(v)
//            }
//            binding.chat.visible()
            binding.connectedContainer.visible()

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

        requireActivity().setTitle("Server")
        Log.d(TAG , "ISSERVER VALUE : "+isServer)
        binding.connectedContainer.visible()
        binding.noDevices.visible()
        binding.progressBar.visible()

        Run.after(500, {
            binding.progressBar.gone()
            binding.noDevices.gone()
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