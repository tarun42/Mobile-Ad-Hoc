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
package com.example.bluetoothlechat.chat

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothlechat.*
import com.example.bluetoothlechat.bluetooth.Message
import com.example.bluetoothlechat.bluetooth.ChatServer
import com.example.bluetoothlechat.databinding.FragmentBluetoothChatBinding

private const val TAG = "BluetoothChatFragment"
class Run {
    companion object {
        fun after(delay: Long, process: () -> Unit) {
            Handler().postDelayed({
                process()
            }, delay)
        }
    }
}
class BluetoothChatFragment : Fragment() {

    var clientDeviceList = listOf<BluetoothDevice>()
    private var _binding: FragmentBluetoothChatBinding? = null
    // this property is valid between onCreateView and onDestroyView.
    private val binding: FragmentBluetoothChatBinding
        get() = _binding!!

    private val deviceConnectionObserver = Observer<DeviceConnectionState> { state ->
        when(state) {
            is DeviceConnectionState.Connected -> {
                val device = state.device
                Log.d(TAG, "Gatt connection observer: have device $device")
                chatWith(device)
            }
            is DeviceConnectionState.Disconnected -> {
                showDisconnected()
            }
        }

    }

    private val connectionRequestObserver = Observer<BluetoothDevice> { device ->
        Log.d(TAG, "Connection request observer: have device $device")
        clientDeviceList = clientDeviceList  + device
        Log.d("TARUN", clientDeviceList!!.toList().toString())
        ChatServer.setCurrentChatConnection(device)         //
    }

    private val messageObserver = Observer<Message> { message ->
        Log.d(TAG, "Have message ${message.text}")
        adapter.addMessage(message)
    }

    private val adapter = MessageAdapter()

    private val inputMethodManager by lazy {
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBluetoothChatBinding.inflate(inflater, container, false)

        Log.d(TAG, "chatWith: set adapter $adapter")
        binding.messages.layoutManager = LinearLayoutManager(context)
        binding.messages.adapter = adapter

        showDisconnected()

        binding.connectDevices.setOnClickListener {
            findNavController().navigate(R.id.action_find_new_device)
        }
        if(firstVisit)
        {
            findNavController().navigate(R.id.action_find_new_device)
            firstVisit = false

        }
        if(server)
        {
            Toast.makeText(context,"IM SERVER",Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        requireActivity().setTitle(R.string.chat_title)
//        binding.connectDevices.gone()
//        binding.connectDeviceMessage.gone()
//        Run.after(10000, {
//            binding.progressBar.gone()
//            binding.textview.gone()
//            binding.connectDevices.visible()
//            binding.connectDeviceMessage.visible()
//        })
//        Run.after(8000,{
//            binding.textview.text = "INITIALIAZING AS SERVER";
//
//        })
        ChatServer.connectionRequest.observe(viewLifecycleOwner, connectionRequestObserver)
        ChatServer.deviceConnection.observe(viewLifecycleOwner, deviceConnectionObserver)
        ChatServer.messages.observe(viewLifecycleOwner, messageObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun chatWith(device: BluetoothDevice) {
        binding.connectedContainer.visible()
        binding.notConnectedContainer.gone()

        val chattingWithString = resources.getString(R.string.chatting_with_device, device.address)
        binding.connectedDeviceName.text = chattingWithString
        binding.sendMessage.setOnClickListener {
            val message = binding.messageText.text.toString()
            // only send message if it is not empty
            Toast.makeText(context,"this is toast",Toast.LENGTH_SHORT).show();

            if (message.isNotEmpty()) {
                //boardcast logic

                    val itr = clientDeviceList.listIterator()
                if(clientDeviceList.size<=1)
                {
                    Toast.makeText(context,"send msg for single connected",Toast.LENGTH_SHORT).show()
                    ChatServer.sendMessage(message)
                }
                else {

                    ChatServer.setCurrentChatConnection(clientDeviceList.get(0))
                    ChatServer.sendMessage("message")
                        Run.after(10, {
                            ChatServer.setCurrentChatConnection(clientDeviceList.get(1))
                            ChatServer.sendMessage("message")
                        })
                        Toast.makeText(context,"While got executed",Toast.LENGTH_SHORT).show()

                }

                // clear message
                binding.messageText.setText("")
            }
        }
    }

    private fun showDisconnected() {
        hideKeyboard()
        binding.notConnectedContainer.visible()
        binding.connectedContainer.gone()
    }

    private fun hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}