package com.example.doprava_bp

import kotlin.Throws
import kotlin.jvm.JvmStatic
import com.example.doprava_bp.AppParameters
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.net.Socket

class Client {
    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            Client()
        }
    }

    init {
        val socket = Socket("127.0.0.1", 3191)
        val objectOutputStream = ObjectOutputStream(socket.getOutputStream())
        val objectInputStream = ObjectInputStream(socket.getInputStream())
        val helloMessage = AppParameters("Hello from App!")
        objectOutputStream.writeObject(helloMessage)
        val appParameters = objectInputStream.readObject() as AppParameters
        println(appParameters.message)
        println(appParameters.atu)
        println(appParameters.hatu)
        println(appParameters.userKey)
        objectOutputStream.close()
        socket.close()
    }
}