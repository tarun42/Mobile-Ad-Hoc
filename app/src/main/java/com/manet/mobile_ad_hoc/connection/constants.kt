package com.manet.mobile_ad_hoc.connection

import android.view.View
import java.util.*

object constants {


    val SERVICE_UUID: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")

    /**
     * UUID for the message
     */
    val MESSAGE_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")
    val SERVER_MESSAGE_UUID: UUID = UUID.fromString("06bf684e-7a96-43ad-9ebc-0c69fe36b191")
    /**
     * UUID to confirm device connection
     */
    val CONFIRM_UUID: UUID = UUID.fromString("36d4dc5c-814b-4097-a5a6-b93b39085928")

    const val REQUEST_ENABLE_BT = 1
    const val LOCATION_REQUEST_CODE = 0
    var isServer : Boolean = false;
    var isScanWorked : Boolean = false;
    var isFirst : Boolean = true;
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

val <T> T.exhaustive: T
    get() = this