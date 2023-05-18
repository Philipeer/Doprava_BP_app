package com.example.doprava_bp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigInteger
import java.net.Socket
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
private val rnd : Random = Random()
const val PREFS_NAME = "MyPrefsFile"

class MainActivity : AppCompatActivity(), NfcHandler.NfcListener {

    private val KEY_LENGTH = 128
    private val TAG = "MainActivity"
    private var myHCEService = MyHCEService()
    private var mContext: Context? = null
    private lateinit var mNfcHandler: NfcHandler
    private var isBound = false

    companion object {
        private const val EXTRA_MESSENGER = "com.example.myapp.EXTRA_MESSENGER"
    }



    fun getContext(): Context? {
        return mContext
    }
    val client = Client()



    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        val intent = Intent(this, MyHCEService::class.java)


        mContext = applicationContext
        mNfcHandler = NfcHandler(this)
        var sharedPreferences = getSharedPreferences("AppParameters", Context.MODE_PRIVATE)
        val sharedPreferences128 = getSharedPreferences("AppParameters128", Context.MODE_PRIVATE)
        val sharedPreferences192 = getSharedPreferences("AppParameters192", Context.MODE_PRIVATE)
        val sharedPreferences256 = getSharedPreferences("AppParameters256", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val editor128 = sharedPreferences128.edit()
        val editor192 = sharedPreferences192.edit()
        val editor256 = sharedPreferences256.edit()
        val appParameters = AppParameters()

        val btnConnection = findViewById<Button>(R.id.btnConnection)
        val btnAuth = findViewById<Button>(R.id.btnAuth)
        val button = findViewById<Button>(R.id.button)
        val btnBleCon = findViewById<Button>(R.id.btnBleCon)
        val tvUserKey = findViewById<TextView>(R.id.tvUserKey)
        val tvAtu = findViewById<TextView>(R.id.tvAtu)
        val tvHatu = findViewById<TextView>(R.id.tvHatu)
        val tvUserNonce = findViewById<TextView>(R.id.tvUserNonce)
        val tvReceiverNonce = findViewById<TextView>(R.id.tvReceiverNonce)
        val tvIdr = findViewById<TextView>(R.id.tvIdr)
        val tvAuthenticated = findViewById<TextView>(R.id.tvAuthenticated)
        val switchLock = findViewById<Switch>(R.id.switchLock) //check -> isChecked()
        var lockStatus = "unlock" // Initial value when Switch is not activated
        val client = Client()
        //lateinit var bluetoothHandler: BluetoothHandler
        lateinit var bluetoothHandler: BluetoothHandlerV2

        if (KEY_LENGTH == 128 && (sharedPreferences128 != null)){
            tvUserKey.text = sharedPreferences128.getString("userKey", null)
            tvHatu.text = sharedPreferences128.getString("hatu", null)
            appParameters.userKey = sharedPreferences128.getString("userKey", null)
            appParameters.hatu = sharedPreferences128.getString("hatu", null)
            appParameters.atu =
                Base64.decode(sharedPreferences128.getString("ATU", null), Base64.DEFAULT)
            appParameters.keyLengths = sharedPreferences128.getInt("keyLenghts", 0)
            tvAtu.text = appParameters.keyLengths.toString()
            intent.putExtra("UserKey", appParameters.userKey)
            intent.putExtra("Hatu", appParameters.hatu)
            intent.putExtra("KeyLength", appParameters.keyLengths)
            intent.putExtra("Atu", appParameters.atu)
            startService(intent)
            Log.i("keyLen:", appParameters.keyLengths.toString())
        }
        else if (KEY_LENGTH == 192 && (sharedPreferences192 != null))
        {
            tvUserKey.text = sharedPreferences192.getString("userKey", null)
            tvHatu.text = sharedPreferences192.getString("hatu", null)
            appParameters.userKey = sharedPreferences192.getString("userKey", null)
            appParameters.hatu = sharedPreferences192.getString("hatu", null)
            appParameters.atu =
                Base64.decode(sharedPreferences192.getString("ATU", null), Base64.DEFAULT)
            appParameters.keyLengths = sharedPreferences192.getInt("keyLenghts", 0)
            tvAtu.text = appParameters.keyLengths.toString()
            intent.putExtra("UserKey", appParameters.userKey)
            intent.putExtra("Hatu", appParameters.hatu)
            intent.putExtra("KeyLength", appParameters.keyLengths)
            intent.putExtra("Atu", appParameters.atu)
            startService(intent)
            Log.i("keyLen:", appParameters.keyLengths.toString())
        }
        else if (KEY_LENGTH == 256 && (sharedPreferences256 != null))
        {
            tvUserKey.text = sharedPreferences256.getString("userKey", null)
            tvHatu.text = sharedPreferences256.getString("hatu", null)
            appParameters.userKey = sharedPreferences256.getString("userKey", null)
            appParameters.hatu = sharedPreferences256.getString("hatu", null)
            appParameters.atu =
                Base64.decode(sharedPreferences256.getString("ATU", null), Base64.DEFAULT)
            appParameters.keyLengths = sharedPreferences256.getInt("keyLenghts", 0)
            tvAtu.text = appParameters.keyLengths.toString()
            intent.putExtra("UserKey", appParameters.userKey)
            intent.putExtra("Hatu", appParameters.hatu)
            intent.putExtra("KeyLength", appParameters.keyLengths)
            intent.putExtra("Atu", appParameters.atu)
            startService(intent)
            Log.i("keyLen:", appParameters.keyLengths.toString())
        }
        else {
            if(sharedPreferences.getString("userKey", null) == null) {
                tvUserKey.text = sharedPreferences.getString("userKey", null)
                tvHatu.text = sharedPreferences.getString("hatu", null)
                appParameters.userKey = sharedPreferences.getString("userKey", null)
                appParameters.hatu = sharedPreferences.getString("hatu", null)
                appParameters.atu =
                    Base64.decode(sharedPreferences.getString("ATU", null), Base64.DEFAULT)
                appParameters.keyLengths = sharedPreferences.getInt("keyLenghts", 0)
                tvAtu.text = appParameters.keyLengths.toString()
                intent.putExtra("UserKey", appParameters.userKey)
                intent.putExtra("Hatu", appParameters.hatu)
                intent.putExtra("KeyLength", appParameters.keyLengths)
                intent.putExtra("Atu", appParameters.atu)
                startService(intent)
                Log.i("keyLen:", appParameters.keyLengths.toString())
            }
            else{
                tvAuthenticated.text = "Je potřeba si vyžádat klíče"
            }
        }


        btnConnection.setOnClickListener {

            if(KEY_LENGTH == 128)
            {
                client.receiveParamsFromIdP()
                editor128.putString("userKey", client.appParameters.userKey)
                editor128.putString("hatu", client.appParameters.hatu)
                editor128.putString("ATU", Base64.encodeToString(client.appParameters.atu, Base64.DEFAULT))
                editor128.putInt("keyLenghts", client.appParameters.keyLengths)
                editor128.apply()
                tvUserKey.text = sharedPreferences128.getString("userKey",null)
                tvHatu.text = sharedPreferences128.getString("hatu",null)
                tvAtu.text = sharedPreferences128.getString("atu",null)
            }
            else if (KEY_LENGTH == 192)
            {
                client.receiveParamsFromIdP()
                editor192.putString("userKey", client.appParameters.userKey)
                editor192.putString("hatu", client.appParameters.hatu)
                editor192.putString("ATU", Base64.encodeToString(client.appParameters.atu, Base64.DEFAULT))
                editor192.putInt("keyLenghts", client.appParameters.keyLengths)
                editor192.apply()
                tvUserKey.text = sharedPreferences192.getString("userKey",null)
                tvHatu.text = sharedPreferences192.getString("hatu",null)
                tvAtu.text = sharedPreferences192.getString("atu",null)
            }
            else{
                client.receiveParamsFromIdP()
                editor256.putString("userKey", client.appParameters.userKey)
                editor256.putString("hatu", client.appParameters.hatu)
                editor256.putString("ATU", Base64.encodeToString(client.appParameters.atu, Base64.DEFAULT))
                editor256.putInt("keyLenghts", client.appParameters.keyLengths)
                editor256.apply()
                tvUserKey.text = sharedPreferences256.getString("userKey",null)
                tvHatu.text = sharedPreferences256.getString("hatu",null)
                tvAtu.text = sharedPreferences256.getString("atu",null)
            }


        }

        btnAuth.setOnClickListener {
            tvUserNonce.text = bluetoothHandler.cryptoCore.userCryptogram.nonce.toString()
            tvReceiverNonce.text = bluetoothHandler.cryptoCore.receiverCryptogram.nonce.toString()
            tvAuthenticated.text = bluetoothHandler.cryptoCore.userCryptogram.isAuthenticated.toString()
        }

        button.setOnClickListener {

            val ip = "172.20.10.6"
            //TCP case
            val nu = rnd.nextInt()
            var userCryptogram = Cryptogram()
            var receiverCryptogram = Cryptogram()
            userCryptogram.nonce = nu
            userCryptogram.hatu = appParameters.hatu

            //výměna nonces
            var socket = Socket(ip, 10001)
            var objectOutputStream = ObjectOutputStream(socket.getOutputStream())
            var objectInputStream = ObjectInputStream(socket.getInputStream())
            objectOutputStream.writeObject(userCryptogram)
            receiverCryptogram = objectInputStream.readObject() as Cryptogram
            objectOutputStream.close() //??
            socket.close()

            //vypocitani ciphertextu a jeho poslání
            var command = "unlock"
            if (switchLock.isChecked) {command = "unlock"}
            else {command = "lock"}
            val cryptoCore = CryptoCore(appParameters,userCryptogram,receiverCryptogram, command)
            cryptoCore.setUserIv()
            userCryptogram.cryptograms.add(cryptoCore.getFinalCipher())
            var socket2 = Socket(ip, 10003)
            objectOutputStream = ObjectOutputStream(socket2.getOutputStream())
            objectOutputStream.writeObject(userCryptogram)
            objectOutputStream.close() //??
            socket2.close()

            //PŘIJMUTÍ VÝSLEDKU AUTENTIZACE

            //pro pc
            val socket3 = Socket(ip, 10004)
            objectOutputStream = ObjectOutputStream(socket3.getOutputStream())
            objectInputStream = ObjectInputStream(socket3.getInputStream())

            //objectOutputStream.writeObject(userCryptogram)
            receiverCryptogram = objectInputStream.readObject() as Cryptogram
            Log.i("TCP Auth:",receiverCryptogram.isAuthenticated.toString())
            socket3.close()

            if(receiverCryptogram.isAuthenticated){
                val intentTcp = Intent(this,AuthActivityTcp::class.java)
                startActivity(intentTcp)
            }
            else {
                val intentTcp = Intent(this,NauthActivityTcp::class.java)
                startActivity(intentTcp)
            }
        }

        btnBleCon.setOnClickListener {

            switchLock.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    lockStatus = "lock"
                } else {
                    lockStatus = "unlock"
                }
            }

            val permission1 = "android.permission.BLUETOOTH_CONNECT"
            val permission2= "android.permission.ACCESS_FINE_LOCATION"
            val permission21= "android.permission.ACCESS_COARSE_LOCATION"
            val permission3= "android.permission.BLUETOOTH_SCAN"
            val permission4 = "android.permission.BLUETOOTH_ADMIN"
            val permission5 = "android.permission.ACCESS_BACKGROUND_LOCATION"
            requestPermissions(arrayOf(permission1,permission2,permission3,permission4,permission21,permission5), ENABLE_BLUETOOTH_REQUEST_CODE)
            //bluetoothHandler.central.connectPeripheral(bluetoothHandler.peripheral,bluetoothHandler.peripheralCallback)
            val SERVICE_UUID = UUID.fromString("18b41747-01df-44d1-bc25-187082eb76bf")
            var command = "unlock"
            if (switchLock.isChecked) {command = "unlock"}
            else {command = "lock"}
            bluetoothHandler = BluetoothHandlerV2(applicationContext,appParameters,command) //ZDE PŘIDĚLAT SWITCHSTATUS
            bluetoothHandler.central.scanForPeripheralsWithServices(arrayOf(
                SERVICE_UUID
            ));
            /*bluetoothHandler = BluetoothHandler(applicationContext,appParameters)
            bluetoothHandler.central.scanForPeripheralsWithServices(arrayOf(
                SERVICE_UUID
            ));*/
            //bluetoothHandler.central.scanForPeripherals()
            //tvIdr.text = bluetoothHandler.receiverCryptogram.idr.toString()
            tvUserNonce.text = bluetoothHandler.userCryptogram.nonce.toString()
            tvReceiverNonce.text = bluetoothHandler.receiverCryptogram.nonce.toString()
            tvAuthenticated.text = bluetoothHandler.userCryptogram.isAuthenticated.toString()







        }
    }


   override fun onResume() {
       super.onResume()
   }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            isBound = false
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("onNewIntent",intent.toString())
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            Log.i("onNewIntent","NfcAdapter.ACTION_TECH_DISCOVERED")
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            myHCEService.transmit(tag,"Test".toByteArray())
        }
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {

            Log.i("onNewIntent","NfcAdapter.ACTION_TAG_DISCOVERED")
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            myHCEService.transmit(tag,"Test".toByteArray())
        }
    }

    override fun onNfcRead(message: String) {
        Log.i("přečtená data NFC:",message)
    }

    override fun onNfcWrite(message: String) {
        Log.i("zapsaná data NFC:",message)
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

    private fun intToBytes(data: Int): ByteArray? {
        return byteArrayOf(
            (data shr 24 and 0xff).toByte(),
            (data shr 16 and 0xff).toByte(),
            (data shr 8 and 0xff).toByte(),
            (data shr 0 and 0xff).toByte()
        )
    }

    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    private fun convertByteArrayToInt(data: ByteArray?): Int {
        return if (data == null || data.size != 4) 0x0 else ( // NOTE: type cast not necessary for int
                0xff and data[0].toInt() shl 24 or (
                        0xff and data[1].toInt() shl 16) or (
                        0xff and data[2].toInt() shl 8) or (
                        0xff and data[3].toInt() shl 0))
        // ----------
    }


}