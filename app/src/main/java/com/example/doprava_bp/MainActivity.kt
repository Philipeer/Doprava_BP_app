package com.example.doprava_bp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Base64
import android.util.Log
import android.widget.*
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

    private val TAG = "MainActivity"
    //private lateinit var nfcAdapter: NfcAdapter
    private var myHCEService = MyHCEService()
    private var mContext: Context? = null
    private lateinit var mNfcHandler: NfcHandler
    private var isBound = false
    private val handler = Handler(Handler.Callback { message ->
        // Get the response message from the service
        val response = message.obj as? ByteArray ?: return@Callback false

        // Update the UI to display the response
        Log.i("handler obj. response:",response.toHex())

        true
    })

    //private val connection = object : ServiceConnection {
    //    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
    //        val binder = service as MyHCEService.LocalBinder
    //        myHCEService = binder.getService()
    //        isBound = true
    //    }
//
//
    //    override fun onServiceDisconnected(name: ComponentName?) {
    //        myHCEService = null
    //        isBound = false
    //    }
    //}

    private val nfcCallback = object : NfcAdapter.ReaderCallback {
        override fun onTagDiscovered(tag: Tag?) {
            val resultIntent = Intent().apply {
                action = "com.example.doprava_bp.TRANSMIT_MESSAGE"
                putExtra("message", "Hello from MainActivity!")
                putExtra("tagId", tag?.id)
            }
            sendBroadcast(resultIntent)
        }
    }

    companion object {
        private const val EXTRA_MESSENGER = "com.example.myapp.EXTRA_MESSENGER"
    }



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

        //val serviceIntent = Intent(this, MyHCEService::class.java)
        //startService(serviceIntent)

        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        //nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // register the broadcast receiver
        //val filter = IntentFilter("com.example.doprava_bp.TRANSMIT_MESSAGE")
        //val receiver = MyBroadcastReceiver()
        //registerReceiver(receiver, filter)
        //if (!nfcAdapter.isEnabled) {
        //    Toast.makeText(this, "NFC is disabled", Toast.LENGTH_LONG).show()
        //}
        val intent = Intent(this, MyHCEService::class.java)
        //bindService(intent, connection, Context.BIND_AUTO_CREATE)

        mContext = applicationContext
        mNfcHandler = NfcHandler(this)
        val sharedPreferences = getSharedPreferences("AppParameters", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val appParameters = AppParameters()

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val checkedId = radioGroup.checkedRadioButtonId
        val btnConnection = findViewById<Button>(R.id.btnConnection)
        val btnAuth = findViewById<Button>(R.id.btnAuth)
        val button = findViewById<Button>(R.id.button)
        //val scan_button = findViewById<Button>(R.id.scan_button)
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

        tvUserKey.text = sharedPreferences.getString("userKey",null)
        tvHatu.text = sharedPreferences.getString("hatu",null)
        appParameters.userKey = sharedPreferences.getString("userKey",null)
        appParameters.hatu = sharedPreferences.getString("hatu",null)
        appParameters.atu = Base64.decode(sharedPreferences.getString("ATU", null), Base64.DEFAULT)
        appParameters.keyLengths = sharedPreferences.getInt("keyLenghts",0)
        tvAtu.text = appParameters.keyLengths.toString()
        intent.putExtra("UserKey",appParameters.userKey)
        intent.putExtra("Hatu",appParameters.hatu)
        intent.putExtra("KeyLength",appParameters.keyLengths)
        startService(intent)
        Log.i("keyLen:",appParameters.keyLengths.toString())

        btnConnection.setOnClickListener {

            client.receiveParamsFromIdP()
            editor.putString("userKey", client.appParameters.userKey)
            editor.putString("hatu", client.appParameters.hatu)
            editor.putString("ATU", Base64.encodeToString(client.appParameters.atu, Base64.DEFAULT))
            editor.putInt("keyLenghts", client.appParameters.keyLengths)
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

        btnAuth.setOnClickListener { //TODO: ZDE VYŘEŠIT NOVOU METODOU PRO VRÁCENÍ STRINGŮ
            //tvIdr.text = bluetoothHandler.cryptoCore.receiverCryptogram.idr.toString()
            tvUserNonce.text = bluetoothHandler.cryptoCore.userCryptogram.nonce.toString()
            tvReceiverNonce.text = bluetoothHandler.cryptoCore.receiverCryptogram.nonce.toString()
            tvAuthenticated.text = bluetoothHandler.cryptoCore.userCryptogram.isAuthenticated.toString()
        }

        button.setOnClickListener {
            //TCP case
            val nu = rnd.nextInt()
            var userCryptogram = Cryptogram()
            var receiverCryptogram = Cryptogram()
            userCryptogram.nonce = nu
            userCryptogram.hatu = appParameters.hatu

            //výměna nonces
            var socket = Socket("10.0.0.72", 10001)
            var objectOutputStream = ObjectOutputStream(socket.getOutputStream())
            var objectInputStream = ObjectInputStream(socket.getInputStream())
            objectOutputStream.writeObject(userCryptogram)
            receiverCryptogram = objectInputStream.readObject() as Cryptogram
            objectOutputStream.close() //??
            socket.close()

            //vypocitani ciphertextu a jeho poslání
            val cryptoCore = CryptoCore(appParameters,userCryptogram,receiverCryptogram)
            cryptoCore.setUserIv()
            userCryptogram.cryptograms.add(cryptoCore.getFinalCipher())
            socket = Socket("10.0.0.72", 10001)
            objectOutputStream = ObjectOutputStream(socket.getOutputStream())
            objectOutputStream.writeObject(userCryptogram)
            objectOutputStream.close() //??
            socket.close()

            //TODO: PŘIJMUTÍ VÝSLEDKU AUTENTIZACE
            socket = Socket("10.0.0.72", 10002)
            objectOutputStream = ObjectOutputStream(socket.getOutputStream())
            objectInputStream = ObjectInputStream(socket.getInputStream())
            objectOutputStream.writeObject(userCryptogram)
            receiverCryptogram = objectInputStream.readObject() as Cryptogram
            Log.i("TCP Auth:",receiverCryptogram.isAuthenticated.toString())
            socket.close()
        }

        //scan_button.setOnClickListener { startBleScan() }
        btnBleCon.setOnClickListener {

            switchLock.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    lockStatus = "lock"
                } else {
                    lockStatus = "unlock"
                }
            }

            if (checkedId != -1) {
                val checkedRadioButton = findViewById<RadioButton>(checkedId)
                when (checkedRadioButton) {
                    findViewById<RadioButton>(R.id.radioBLE) -> {
                        val permission1 = "android.permission.BLUETOOTH_CONNECT"
                        val permission2= "android.permission.ACCESS_FINE_LOCATION"
                        val permission21= "android.permission.ACCESS_COARSE_LOCATION"
                        val permission3= "android.permission.BLUETOOTH_SCAN"
                        val permission4 = "android.permission.BLUETOOTH_ADMIN"
                        val permission5 = "android.permission.ACCESS_BACKGROUND_LOCATION"
                        requestPermissions(arrayOf(permission1,permission2,permission3,permission4,permission21,permission5), ENABLE_BLUETOOTH_REQUEST_CODE)
                        //bluetoothHandler.central.connectPeripheral(bluetoothHandler.peripheral,bluetoothHandler.peripheralCallback)
                        val SERVICE_UUID = UUID.fromString("18b41747-01df-44d1-bc25-187082eb76bf")
                        bluetoothHandler = BluetoothHandlerV2(applicationContext,appParameters) //ZDE PŘIDĚLAT SWITCHSTATUS
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
                    findViewById<RadioButton>(R.id.radioWifi) -> {
                        //TCP case
                        val nu = rnd.nextInt()
                        var userCryptogram = Cryptogram()
                        var receiverCryptogram = Cryptogram()
                        userCryptogram.nonce = nu
                        userCryptogram.hatu = appParameters.hatu

                        //výměna nonces
                        var socket = Socket("10.0.0.72", 10001)
                        var objectOutputStream = ObjectOutputStream(socket.getOutputStream())
                        var objectInputStream = ObjectInputStream(socket.getInputStream())
                        objectOutputStream.writeObject(userCryptogram)
                        receiverCryptogram = objectInputStream.readObject() as Cryptogram
                        objectOutputStream.close() //??
                        socket.close()

                        //vypocitani ciphertextu a jeho poslání
                        val cryptoCore = CryptoCore(appParameters,userCryptogram,receiverCryptogram)
                        cryptoCore.setUserIv()
                        userCryptogram.cryptograms.add(cryptoCore.getFinalCipher())
                        socket = Socket("10.0.0.72", 10001)
                        objectOutputStream = ObjectOutputStream(socket.getOutputStream())
                        objectOutputStream.writeObject(userCryptogram)
                        objectOutputStream.close() //??
                        socket.close()

                        //TODO: PŘIJMUTÍ VÝSLEDKU AUTENTIZACE
                        socket = Socket("10.0.0.72", 10002)
                        objectOutputStream = ObjectOutputStream(socket.getOutputStream())
                        objectInputStream = ObjectInputStream(socket.getInputStream())
                        objectOutputStream.writeObject(userCryptogram)
                        receiverCryptogram = objectInputStream.readObject() as Cryptogram
                        Log.i("TCP Auth:",receiverCryptogram.isAuthenticated.toString())
                        socket.close()
                    }
                    findViewById<RadioButton>(R.id.radioNFC) -> {
                        // Handle the case when the "NFC" radio button is selected
                        // Perform the appropriate action here
                    }
                    else -> {
                        // Handle other cases when a RadioButton other than "BLE", "WiFi", or "NFC" is selected
                        // Perform the appropriate action here
                    }
                }
            } else {
                // No RadioButton is checked
            }





        }
    }


   override fun onResume() {
       super.onResume()
       //val pendingIntent = PendingIntent.getActivity(this, 0,
       //    Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
       //val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
       //val techLists = arrayOf(arrayOf(NfcF::class.java.name))
       //nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techLists)
       //nfcAdapter.enableReaderMode(this, nfcCallback, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
   }

    override fun onPause() {
        super.onPause()
        //nfcAdapter.disableForegroundDispatch(this)
        //nfcAdapter.disableReaderMode(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            //unbindService(connection)
            isBound = false
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("onNewIntent",intent.toString())
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            Log.i("onNewIntent","NfcAdapter.ACTION_TECH_DISCOVERED")
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            //val myHceService = MyHCEService().LocalBinder().service
            myHCEService.transmit(tag,"Test".toByteArray())
        }
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            // Get the Tag object from the intent

            //val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) ?: return
//
            //// Create a new instance of MyHCEService and bind to it
            //val hceIntent = Intent(this, MyHCEService::class.java)
            //val messenger = Messenger(handler)
            //hceIntent.putExtra(EXTRA_MESSENGER, messenger)
            //startService(hceIntent)
            //
//
            //// Get the IsoDep object and connect to the tag
            //val isoDep = IsoDep.get(tag)
            //isoDep.connect()
//
            //// Send an APDU command to the HCE service
            //val command = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, 0x07, 0xA0.toByte(), 0x00, 0x00, 0x00, 0x03, 0x10, 0x10)
            //val response = isoDep.transceive(command)
//
            //// Close the IsoDep connection
            //isoDep.close()
            Log.i("onNewIntent","NfcAdapter.ACTION_TAG_DISCOVERED")
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            //val myHceService = MyHCEService().LocalBinder().service
            myHCEService.transmit(tag,"Test".toByteArray())
            //val pokus = "Ahoj".toByteArray()
            //MyHCEService.getInstance().transmit(pokus)
        }
        //mNfcHandler.handleIntent(intent, this)
    }

    override fun onNfcRead(message: String) {
        Log.i("přečtená data NFC:",message)
    }

    override fun onNfcWrite(message: String) {
        Log.i("zapsaná data NFC:",message)
    }



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

    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

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