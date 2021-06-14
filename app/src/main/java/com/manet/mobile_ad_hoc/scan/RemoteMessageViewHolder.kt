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

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.manet.mobile_ad_hoc.R
import com.manet.mobile_ad_hoc.connection.constants
import com.manet.mobile_ad_hoc.scan.Message

class RemoteMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val messageText = itemView.findViewById<TextView>(R.id.message_text)
    private val sourceText = itemView.findViewById<TextView>(R.id.source)
    private val routeText = itemView.findViewById<TextView>(R.id.route)

    fun bind(message: Message.RemoteMessage) {
        var ind1 = message.text.indexOf(":")

        var msg = message.text.substring(ind1+1)
        var ind2 = msg.indexOf(":")
        Log.d("=======================",message.text.substring(ind1+1))
        messageText.text = message.text.slice(0..ind1-1)
        sourceText.text = msg.slice(0..ind2-1)
        routeText.text = msg.substring(ind2+1)
        Log.d("=======================",msg.substring(ind2+1))
//        constants.globalStr = "";
    }
}