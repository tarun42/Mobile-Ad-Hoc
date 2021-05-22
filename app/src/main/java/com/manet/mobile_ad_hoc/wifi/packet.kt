package com.manet.wifidirect

data class packet(var message: String = "",
                  var source : String = "",
                  var dest : String = "" ,
                  var route : String = "" ,
                  var hopcount : Int? = null) {}
