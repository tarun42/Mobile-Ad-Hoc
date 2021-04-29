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

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.*
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.manet.mobile_ad_hoc.DeviceScanViewState
import com.manet.mobile_ad_hoc.connection.constants.SERVICE_UUID
import com.manet.mobile_ad_hoc.scan.Message
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


private const val TAG = "DeviceScanViewModel"
// 30 second scan period
private const val SCAN_PERIOD = 5000L
private val scanResults = mutableMapOf<String, BluetoothDevice>()

class DeviceScanViewModel(app: Application) : AndroidViewModel(app) {

    var realDeviceName : String? = null
    // LiveData for sending the view state to the DeviceScanFragment
    private val _viewState = MutableLiveData<DeviceScanViewState>()
    val viewState = _viewState as LiveData<DeviceScanViewState>

    // String key is the address of the bluetooth device
    private val repeatObserver= Observer<Int> { num ->
        Log.d("TAG", "Int"+num)
//        binding.textview.text = message.toString()
        startScan()
    }

    // BluetoothAdapter should never be null since BLE is required per
    // the <uses-feature> tag in the AndroidManifest.xml
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // This property will be null if bluetooth is not enabled
    private var scanner: BluetoothLeScanner? = null

    private var scanCallback: DeviceScanCallback? = null
    private var scanFilters: List<ScanFilter>
    private var scanSettings: ScanSettings
    var isFirst : Boolean = true;
    init {
        // Setup scan filters and settings
        scanFilters = buildScanFilters()
        scanSettings = buildScanSettings()

        // Start a scan for BLE devices
        startScan()
        isFirst = false
    }
    private fun startScancopy(){
        scanFilters = buildScanFilters()
        scanSettings = buildScanSettings()

        // Start a scan for BLE devices
        startScan()
    }


    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }
//    fun becomeServer()
//    {
//        realDeviceName = adapter.name;
//        adapter.setName("MANET-SERVER "+realDeviceName);
//        Log.d("DeviceScanFragment","became server")
//    }
//    fun becomeClient()
//    {
//        adapter.setName(realDeviceName);
//        Log.d("DeviceScanFragment","became client")
//    }
    fun startScanAgain()
    {
        scanFilters = buildScanFilters()
        scanSettings = buildScanSettings()

        // Start a scan for BLE devices
        startScan()
    }
    fun startScan() {
        // If advertisement is not supported on this device then other devices will not be able to
        // discover and connect to it.
        if (!adapter.isMultipleAdvertisementSupported) {
            _viewState.value = DeviceScanViewState.AdvertisementNotSupported
            return
        }

        if (scanCallback == null) {
            scanner = adapter.bluetoothLeScanner
            Log.d(TAG, "Start Scanning")
            // Update the UI to indicate an active scan is starting

            if(isFirst)
                _viewState.value = DeviceScanViewState.ActiveScan

            // Stop scanning after the scan period
            Handler().postDelayed({ stopScanning() }, SCAN_PERIOD)

            // Kick off a new scan
            scanCallback = DeviceScanCallback()
            scanner?.startScan(scanFilters, scanSettings, scanCallback)
        } else {
            Log.d(TAG, "Already scanning")
        }
    }

    private fun stopScanning() {
        Log.d(TAG, "Stopping Scanning")
        scanner?.stopScan(scanCallback)
        scanCallback = null
        // return the current results
        _viewState.value = DeviceScanViewState.ScanResults(scanResults)
    }

    /**
     * Return a List of [ScanFilter] objects to filter by Service UUID.
     */
    private fun buildScanFilters(): List<ScanFilter> {
        val builder = ScanFilter.Builder()
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(ParcelUuid(SERVICE_UUID))
        val filter = builder.build()
        return listOf(filter)
    }

    /**
     * Return a [ScanSettings] object set to use low power (to preserve battery life).
     */
    private fun buildScanSettings(): ScanSettings {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
    }

    /**
     * Custom ScanCallback object - adds found devices to list on success, displays error on failure.
     */
    private inner class DeviceScanCallback : ScanCallback() {
        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            Log.d(TAG," [onBatchScanResults] callback got called ")
            for (item in results) {
                item.device?.let { device ->
//                    if(device.name != "realme Narzo 10")
                        scanResults[device.address] = device
                }
            }
            if(scanResults.isEmpty() && isFirst)
                _viewState.value = DeviceScanViewState.ActiveScan;
            else
                _viewState.value = DeviceScanViewState.ScanResults(scanResults)
        }

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            super.onScanResult(callbackType, result)
            Log.d(TAG," [onScanResult] callback got called ")
                result.device?.let { device ->
//                if(device.name != "realme Narzo 10")
                    scanResults[device.address] = device
            }
            Log.d(TAG , "Size of ScanResult : "+ scanResults.size)
            if(scanResults.isEmpty() && isFirst)
                _viewState.value = DeviceScanViewState.ActiveScan;
            else
                _viewState.value = DeviceScanViewState.ScanResults(scanResults)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Send error state to the fragment to display
            Log.d(TAG," [onScanFailed] callback got called ")
            val errorMessage = "Scan failed with error: $errorCode"
            _viewState.value = DeviceScanViewState.Error(errorMessage)
        }
    }


}