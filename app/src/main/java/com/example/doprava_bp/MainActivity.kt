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
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


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
            val ukey = hash(client.appParameters.userKey + "user" + client.userCryptogram.nonce.toString() +
                    client.receiverCryptogram.nonce.toString(),"SHA-1")
            val plaintextString = client.appParameters.hatu + client.receiverCryptogram.idr + client.userCryptogram.nonce.toString() +
                    client.receiverCryptogram.nonce.toString()
            val plaintext: ByteArray = plaintextString.toByteArray()
            //val keygen = KeyGenerator.getInstance("AES")
           // keygen.init(256)
            //val key: SecretKey = keygen.generateKey()
            val cipher = Cipher.getInstance("AES/GCM")
            cipher.init(Cipher.ENCRYPT_MODE, ukey as SecretKey)
            val ciphertext: ByteArray = cipher.doFinal(plaintext)
            val iv: ByteArray = cipher.iv
        }
    }

    fun hash(input: String, hashType: String?): String? {
        return try {
            // getInstance() method is called with algorithm SHA-1
            val md: MessageDigest = MessageDigest.getInstance(hashType)

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            val messageDigest: ByteArray = md.digest(input.toByteArray())

            // Convert byte array into signum representation
            val no = BigInteger(1, messageDigest)

            // Convert message digest into hex value

            // Add preceding 0s to make it 32 bit
            /*while (hashtext.length() < 32) {
                     hashtext = "0" + hashtext;
                 }*/
            //

            // return the HashText
            no.toString(16)
        } // For specifying wrong message digest algorithms
        catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
}