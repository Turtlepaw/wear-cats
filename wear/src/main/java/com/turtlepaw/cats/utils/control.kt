package com.turtlepaw.cats.utils

class ImageControls {
    var current = 0

    fun increase(){
        current = current.plus(1)
    }

    fun decrease(){
        current = current.minus(1)
    }

    fun setValue(newValue: Int){
        current = newValue
    }
}