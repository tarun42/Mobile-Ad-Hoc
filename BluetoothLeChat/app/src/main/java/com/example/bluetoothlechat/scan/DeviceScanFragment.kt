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
package com.example.bluetoothlechat.scan

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothlechat.*
import com.example.bluetoothlechat.bluetooth.ChatServer
import com.example.bluetoothlechat.bluetooth.Message
import com.example.bluetoothlechat.chat.Run
import com.example.bluetoothlechat.databinding.FragmentDeviceScanBinding
import com.example.bluetoothlechat.scan.DeviceScanViewState.*

private const val TAG = "DeviceScanFragment"
const val GATT_KEY = "gatt_bundle_key"

class DeviceScanFragment : Fragment() {

    private var _binding: FragmentDeviceScanBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding
        get() = _binding!!

    private val viewModel: DeviceScanViewModel by viewModels()

    private val deviceScanAdapter by lazy {
        DeviceScanAdapter(onDeviceSelected)
    }

    private val viewStateObserver = Observer<DeviceScanViewState> { state ->
        when (state) {
            is ActiveScan -> showLoading()
            is ScanResults -> showResults(state.scanResults)
            is Error -> showError(state.message)
            is AdvertisementNotSupported -> showAdvertisingError()
        }.exhaustive
    }

    private val onDeviceSelected: (BluetoothDevice) -> Unit = { device ->
        ChatServer.setCurrentChatConnection(device)
        // navigate back to chat fragment
        findNavController().popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeviceScanBinding.inflate(inflater, container, false)

        binding.deviceList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceScanAdapter
        }


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        Toast.makeText(context,"this is DeviceScanActivity",Toast.LENGTH_SHORT).show()

        requireActivity().setTitle(R.string.device_list_title)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.viewState.observe(viewLifecycleOwner, viewStateObserver)
    }

    private fun showLoading() {
        Log.d(TAG, "showLoading")
        binding.scanning.visible()

        binding.deviceList.gone()
        binding.noDevices.gone()
        binding.error.gone()
        binding.chatConfirmContainer.gone()
    }

    private fun showResults(scanResults: Map<String, BluetoothDevice>) {
        if (scanResults.isNotEmpty()) {
            //binding.deviceList.visible()
            //deviceScanAdapter.updateItems(scanResults.values.toList())

                connect(scanResults.values.toList())

            Toast.makeText(context, scanResults.values.toList().toString(),Toast.LENGTH_SHORT).show()
            binding.scanning.gone()
            binding.noDevices.gone()
            binding.error.gone()
            binding.chatConfirmContainer.gone()
        } else {
            showNoDevices()
        }
    }

    private fun showNoDevices() {
        binding.noDevices.visible()

        Toast.makeText(context,"not found",Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
        server = true

        binding.deviceList.gone()
        binding.scanning.gone()
        binding.error.gone()
        binding.chatConfirmContainer.gone()
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
    private fun connect(list : List<BluetoothDevice>) {
//        firstVisit = false;
        var bluetoothDeviceList = listOf<BluetoothDevice>()
        bluetoothDeviceList = list
        var bluetoothDevice = bluetoothDeviceList.get(0)
        for (item in bluetoothDeviceList)
        {
            if(item.name == "realme#Pro")
            {
                Toast.makeText(context,"FOUND REALME",Toast.LENGTH_SHORT).show()
                bluetoothDevice = item
            }
        }
        if(bluetoothDeviceList.size == 2){
            Toast.makeText(context,"IM SERVER COZ I HAVE @ DEVICES LIST",Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(context,"device found connectig : "+bluetoothDevice.name,Toast.LENGTH_SHORT).show()
        onDeviceSelected(bluetoothDevice)

    }
}