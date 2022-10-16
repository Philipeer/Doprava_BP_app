package com.example.doprava_bp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {


    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        val btnConnection = findViewById<Button>(R.id.btnConnection)
        val btnAuth = findViewById<Button>(R.id.btnAuth)
        val tvUserKey = findViewById<TextView>(R.id.tvUserKey)
        val tvAtu = findViewById<TextView>(R.id.tvAtu)
        val tvHatu = findViewById<TextView>(R.id.tvHatu)
        val tvUserNonce = findViewById<TextView>(R.id.tvUserNonce)
        val tvReceiverNonce = findViewById<TextView>(R.id.tvReceiverNonce)
        val client = Client()

        btnConnection.setOnClickListener {

            client.receiveParamsFromIdP()
            tvUserKey.text = client.appParameters.userKey
            tvHatu.text = client.appParameters.hatu
            tvAtu.text = client.appParameters.atu.toString()
            /*
            val socket = Socket("192.168.56.1", 10001)
            val objectOutputStream = ObjectOutputStream(socket.getOutputStream())
            val objectInputStream = ObjectInputStream(socket.getInputStream())
            val helloMessage = AppParameters("Hello from App!")
            objectOutputStream.writeObject(helloMessage)
            val appParameters = objectInputStream.readObject() as AppParameters
            tvUserKey.text = appParameters.userKey
            //tvAtu.text = appParameters.atu.toString()
            tvHatu.text = appParameters.hatu
            objectOutputStream.close()
            socket.close()

             */
        }

        btnAuth.setOnClickListener {
            client.sendAndReceiveObject()
            tvUserNonce.text = client.userCryptogram.nonce.toString()
            tvReceiverNonce.text = client.receiverCryptogram.nonce.toString()
        }
    }
}