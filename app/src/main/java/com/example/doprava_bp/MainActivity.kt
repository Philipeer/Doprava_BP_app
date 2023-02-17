package com.example.doprava_bp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
private val rnd : Random = Random()
const val PREFS_NAME = "MyPrefsFile"

class MainActivity : AppCompatActivity() {

    private var mContext: Context? = null


    fun getContext(): Context? {
        return mContext
    }
    val client = Client()
   // private val secureRandom: SecureRandom = SecureRandom()
   // private val GCM_IV_LENGTH = 12
   //private val userCryptogram: Cryptogram = Cryptogram()
   //private val receiverCryptogram : Cryptogram = Cryptogram()
   // private var ukeyString = hash(client.appParameters.userKey + "user" + userCryptogram.nonce.toString() +
   // receiverCryptogram.nonce.toString(),"SHA-1")
   // var plaintextString = client.appParameters.hatu + receiverCryptogram.idr + userCryptogram.nonce.toString() +
   //         receiverCryptogram.nonce.toString()
   // var plaintext: ByteArray = plaintextString.toByteArray()
   // val ukey: SecretKey = SecretKeySpec(ukeyString!!.toByteArray(), "AES")




    // Z PUNCHLINE

   // private val bluetoothAdapter: BluetoothAdapter by lazy {
   //     val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
   //     bluetoothManager.adapter
   // }
   //
   // private val bleScanner by lazy {
   //     bluetoothAdapter.bluetoothLeScanner
   // }
   //
   // private val scanSettings = ScanSettings.Builder()
   //     .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
   //     .build()
   //
   // val isLocationPermissionGranted
   //     get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    // PROMĚNNÉ Z BLESSED



    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        mContext = applicationContext
        val sharedPreferences = getSharedPreferences("AppParameters", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val appParameters = AppParameters()

        val btnConnection = findViewById<Button>(R.id.btnConnection)
        val btnAuth = findViewById<Button>(R.id.btnAuth)
        //val scan_button = findViewById<Button>(R.id.scan_button)
        val btnBleCon = findViewById<Button>(R.id.btnBleCon)
        val tvUserKey = findViewById<TextView>(R.id.tvUserKey)
        val tvAtu = findViewById<TextView>(R.id.tvAtu)
        val tvHatu = findViewById<TextView>(R.id.tvHatu)
        val tvUserNonce = findViewById<TextView>(R.id.tvUserNonce)
        val tvReceiverNonce = findViewById<TextView>(R.id.tvReceiverNonce)
        val tvIdr = findViewById<TextView>(R.id.tvIdr)
        val tvAuthenticated = findViewById<TextView>(R.id.tvAuthenticated)
        val client = Client()
        lateinit var bluetoothHandler: BluetoothHandler

        tvUserKey.text = sharedPreferences.getString("userKey",null)
        tvHatu.text = sharedPreferences.getString("hatu",null)
        tvAtu.text = sharedPreferences.getString("atu",null)
        appParameters.userKey = sharedPreferences.getString("userKey",null)
        appParameters.hatu = sharedPreferences.getString("hatu",null)
        appParameters.atu = Base64.decode(sharedPreferences.getString("ATU", null), Base64.DEFAULT)

        btnConnection.setOnClickListener {

            client.receiveParamsFromIdP()
            editor.putString("userKey", client.appParameters.userKey)
            editor.putString("hatu", client.appParameters.hatu)
            editor.putString("ATU", Base64.encodeToString(client.appParameters.atu, Base64.DEFAULT))
            editor.apply()
            tvUserKey.text = sharedPreferences.getString("userKey",null)
            tvHatu.text = sharedPreferences.getString("hatu",null)
            tvAtu.text = sharedPreferences.getString("atu",null)
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
            tvIdr.text = bluetoothHandler.cryptoCore.receiverCryptogram.idr.toString()
            tvUserNonce.text = bluetoothHandler.cryptoCore.userCryptogram.nonce.toString()
            tvReceiverNonce.text = bluetoothHandler.cryptoCore.receiverCryptogram.nonce.toString()
            tvAuthenticated.text = bluetoothHandler.cryptoCore.userCryptogram.isAuthenticated.toString()
        }

        //scan_button.setOnClickListener { startBleScan() }
        btnBleCon.setOnClickListener {
            val permission1 = "android.permission.BLUETOOTH_CONNECT"
            val permission2= "android.permission.ACCESS_FINE_LOCATION"
            val permission21= "android.permission.ACCESS_COARSE_LOCATION"
            val permission3= "android.permission.BLUETOOTH_SCAN"
            val permission4 = "android.permission.BLUETOOTH_ADMIN"
            val permission5 = "android.permission.ACCESS_BACKGROUND_LOCATION"
            requestPermissions(arrayOf(permission1,permission2,permission3,permission4,permission21,permission5), ENABLE_BLUETOOTH_REQUEST_CODE)
            //bluetoothHandler.central.connectPeripheral(bluetoothHandler.peripheral,bluetoothHandler.peripheralCallback)
            val SERVICE_UUID = UUID.fromString("18b41747-01df-44d1-bc25-187082eb76bf")
            bluetoothHandler = BluetoothHandler(applicationContext,appParameters)
            bluetoothHandler.central.scanForPeripheralsWithServices(arrayOf(
                SERVICE_UUID
            ));
            //bluetoothHandler.central.scanForPeripherals()
            tvIdr.text = bluetoothHandler.receiverCryptogram.idr.toString()
            tvUserNonce.text = bluetoothHandler.userCryptogram.nonce.toString()
            tvReceiverNonce.text = bluetoothHandler.receiverCryptogram.nonce.toString()
            tvAuthenticated.text = bluetoothHandler.userCryptogram.isAuthenticated.toString()

        }
    }

  // override fun onResume() {
  //     super.onResume()
  //     if (!bluetoothAdapter.isEnabled) {
  //         promptEnableBluetooth()
  //     }
  // }
  //
  // override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  //     super.onActivityResult(requestCode, resultCode, data)
  //     when (requestCode) {
  //         ENABLE_BLUETOOTH_REQUEST_CODE -> {
  //             if (resultCode != Activity.RESULT_OK) {
  //                 promptEnableBluetooth()
  //             }
  //         }
  //     }
  // }

  //  override fun onRequestPermissionsResult(
  //      requestCode: Int,
  //      permissions: Array<out String>,
  //      grantResults: IntArray
  //  ) {
  //      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  //      when (requestCode) {
  //          LOCATION_PERMISSION_REQUEST_CODE -> {
  //              if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
  //                  requestLocationPermission()
  //              } else {
  //                  startBleScan()
  //              }
  //          }
  //      }
  //  }
  //
  //  @SuppressLint("MissingPermission")
  //  private fun promptEnableBluetooth() {
  //      if (!bluetoothAdapter.isEnabled) {
  //          val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
  //          startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
  //      }
  //  }

   // @SuppressLint("MissingPermission")
   // private fun startBleScan() {
   //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
   //         requestLocationPermission()
   //     }
   //     else { bleScanner.startScan(null, scanSettings, scanCallback) }
   // }
   //
   // private fun requestLocationPermission() {
   //     if (isLocationPermissionGranted) {
   //         return
   //     }
   //     runOnUiThread { /* //TODO: Alerts dont work atm
   //         alert {
   //             title = "Location permission required"
   //             message = "Starting from Android M (6.0), the system requires apps to be granted " +
   //                     "location access in order to scan for BLE devices."
   //             isCancelable = false
   //             positiveButton(android.R.string.ok) {
   //                 requestPermission(
   //                     Manifest.permission.ACCESS_FINE_LOCATION,
   //                     LOCATION_PERMISSION_REQUEST_CODE
   //                 )
   //             }
   //         }.show() ***/
   //     }
   // }

  // private val scanCallback = object : ScanCallback() {
  //     @SuppressLint("MissingPermission")
  //     override fun onScanResult(callbackType: Int, result: ScanResult) {
  //         with(result.device) {
  //             Log.i("ScanCallback", "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
  //         }
  //     }
  // }
  //
  // fun Context.hasPermission(permissionType: String): Boolean {
  //     return ContextCompat.checkSelfPermission(this, permissionType) ==
  //             PackageManager.PERMISSION_GRANTED
  // }
  //
  // private fun Activity.requestPermission(permission: String, requestCode: Int) {
  //     ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
  // }

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

    private fun convertByteArrayToInt(data: ByteArray?): Int {
        return if (data == null || data.size != 4) 0x0 else ( // NOTE: type cast not necessary for int
                0xff and data[0].toInt() shl 24 or (
                        0xff and data[1].toInt() shl 16) or (
                        0xff and data[2].toInt() shl 8) or (
                        0xff and data[3].toInt() shl 0))
        // ----------
    }


    /*
    fun scanForDevices(context : Context, handler: Handler){
        val adapter = BluetoothAdapter.getDefaultAdapter()

        if(!adapter.isEnabled){
            return handler(ScanEvent.ScanError, null, "Bluetooth interface disabled")
        }

        context.runWithPermissions(Manifest.permission.ACCES_COARSE_LOCATION){
            adapter.bluetoothLeScanner.startScan(callback)
        }

    }*/
}