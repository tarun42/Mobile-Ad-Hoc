package com.manet.mobile_ad_hoc.connection

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.manet.mobile_ad_hoc.DeviceConnectionState
import com.manet.mobile_ad_hoc.Run
import com.manet.mobile_ad_hoc.connection.constants.CONFIRM_UUID
import com.manet.mobile_ad_hoc.connection.constants.MESSAGE_UUID
import com.manet.mobile_ad_hoc.connection.constants.SERVER_MESSAGE_UUID
import com.manet.mobile_ad_hoc.connection.constants.SERVICE_UUID
import com.manet.mobile_ad_hoc.connection.constants.isServer
import com.manet.mobile_ad_hoc.scan.Message
import java.util.*

private const val TAG = "Server"

class Run {
    companion object {
        fun after(delay: Long, process: () -> Unit) {
            Handler().postDelayed({
                process()
            }, delay)
        }
    }
}
object Server {

    // hold reference to app context to run the chat server
    private var app: Application? = null
    private lateinit var bluetoothManager: BluetoothManager

    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // This property will be null if bluetooth is not enabled or if advertising is not

    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var advertiseSettings: AdvertiseSettings = buildAdvertiseSettings()
    private var advertiseData: AdvertiseData = buildAdvertiseData()

    // LiveData for reporting the messages sent to the device
    private val _messages = MutableLiveData<Message>()
    val messages = _messages as LiveData<Message>

    // LiveData for reporting connection requests
    private val _connectionRequest = MutableLiveData<BluetoothDevice>()
    val connectionRequest = _connectionRequest as LiveData<BluetoothDevice>

    // LiveData for reporting the messages sent to the device
    private val _requestEnableBluetooth = MutableLiveData<Boolean>()
    val requestEnableBluetooth = _requestEnableBluetooth as LiveData<Boolean>

    private var gattServer: BluetoothGattServer? = null
    private var gattServerCallback: BluetoothGattServerCallback? = null

    private var gattClient: BluetoothGatt? = null
    private var gattClientCallback: BluetoothGattCallback? = null

    // Properties for current chat device connection
    private var currentDevice: BluetoothDevice? = null

    private val _deviceConnection = MutableLiveData<DeviceConnectionState>()
    val deviceConnection = _deviceConnection as LiveData<DeviceConnectionState>

    private var gatt: BluetoothGatt? = null
    private var messageCharacteristic: BluetoothGattCharacteristic? = null


    var t1: Long = System.currentTimeMillis()
    fun startServer(app: Application) {
        bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (!adapter.isEnabled) {
            // prompt the user to enable bluetooth
            _requestEnableBluetooth.value = true
        } else {
            _requestEnableBluetooth.value = false
            setupGattServer(app)
            startAdvertisement()
        }
    }

//    fun stopServer() {
//        stopAdvertising()
//    }

    fun setCurrentChatConnection(device: BluetoothDevice) {
        currentDevice = device
        // Set gatt so BluetoothChatFragment can display the device data
//        _deviceConnection.value = DeviceConnectionState.Connected(device)
        _deviceConnection.postValue(DeviceConnectionState.Connected(device))
        connectToChatDevice(device)
    }


    private fun connectToChatDevice(device: BluetoothDevice) {
        gattClientCallback = GattClientCallback()
        gattClient = device.connectGatt(app, false, gattClientCallback)
    }

    fun sendMessage(message: String): Boolean {
        Log.d(TAG, "Send a message")
        messageCharacteristic?.let { characteristic ->
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            val messageBytes = message.toByteArray(Charsets.UTF_8)
            characteristic.value = messageBytes
            gatt?.let {
                val success = it.writeCharacteristic(messageCharacteristic)
                Log.d(TAG, "onServicesDiscovered: message send: $success")
                if (success) {
//                    _messages.value = Message.LocalMessage(message)
                    _messages.postValue(Message.LocalMessage(message))
                    t1= System.currentTimeMillis()
//                    Run.after(2000, {
//
//                    })
                }
            } ?: run {
                Log.d(TAG, "sendMessage: no gatt connection to send a message with")
            }
        }
        return false
    }


    private fun setupGattServer(app: Application) {
        gattServerCallback = GattServerCallback()

        gattServer = bluetoothManager.openGattServer(
                app,
                gattServerCallback
        ).apply {
            addService(setupGattService())
        }
    }


    private fun setupGattService(): BluetoothGattService {
        // Setup gatt service
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        // need to ensure that the property is writable and has the write permission
        val messageCharacteristic = BluetoothGattCharacteristic(
                MESSAGE_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageCharacteristic)
        val messageServerCharacteristic = BluetoothGattCharacteristic(
                SERVER_MESSAGE_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageServerCharacteristic)
        val confirmCharacteristic = BluetoothGattCharacteristic(
                CONFIRM_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(confirmCharacteristic)

        return service
    }


    private fun startAdvertisement() {
        advertiser = adapter.bluetoothLeAdvertiser
        Log.d(TAG, "startAdvertisement: with advertiser $advertiser")

        if (advertiseCallback == null) {
            advertiseCallback = DeviceAdvertiseCallback()

            advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        }
    }


//    private fun stopAdvertising() {
//        Log.d(TAG, "Stopping Advertising with advertiser $advertiser")
//        advertiser?.stopAdvertising(advertiseCallback)
//        advertiseCallback = null
//    }


    private fun buildAdvertiseData(): AdvertiseData {

        val dataBuilder = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(SERVICE_UUID))
                .setIncludeDeviceName(true)


        return dataBuilder.build()
    }


    private fun buildAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTimeout(0)
                .build()
    }

    private class GattServerCallback : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(
                    TAG,
                    "onConnectionStateChange: Server $device ${device.name} success: $isSuccess connected: $isConnected"
            )
            if (isSuccess && isConnected) {
                _connectionRequest.postValue(device)
                Log.d("TAG", "GOT CONNECTION REQUEST")
            } else {
                Log.d("TAG", "GOT DIS-CONNECTION REQUEST")
                _deviceConnection.postValue(DeviceConnectionState.Disconnected)
            }
        }

        override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            if (characteristic.uuid == MESSAGE_UUID) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                Log.d(TAG, "onCharacteristicWriteRequest: Have message: \"$message\"")
                message?.let {
                    _messages.postValue(Message.RemoteMessage(it))

                    if(isServer)
                    {
                        Log.d(TAG, "RESPONSE MSG FROM SERVER IS SENDING...")
//                        setCurrentChatConnection(device)
                        _deviceConnection.postValue(DeviceConnectionState.Connected(device))
                        connectToChatDevice(device)
                        sendMessage(device.name + " CONFIRMATION ")
                    }
                }

            }
            else
            {
                // if message received from unregister user
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                Log.d(TAG, "onCharacteristicWriteRequest: Have message not from register user: \"$message\"")
            }
        }
    }

    private class GattClientCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(TAG, "onConnectionStateChange: Client $gatt  success: $isSuccess connected: $isConnected")

            if (isSuccess && isConnected) {
                // discover services
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(discoveredGatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered: Have gatt $discoveredGatt")
                gatt = discoveredGatt
                val service = discoveredGatt.getService(SERVICE_UUID)
                messageCharacteristic = service.getCharacteristic(MESSAGE_UUID)

            }
        }
    }


    private class DeviceAdvertiseCallback : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            // Send error state to display
            val errorMessage = "Advertise failed with error: $errorCode"
            Log.d(TAG, "Advertising failed")
            //_viewState.value = DeviceScanViewState.Error(errorMessage)
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully started")
        }
    }
}