package com.example.doprava_bp

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.welie.blessed.*
import java.util.*


class BluetoothHandlerV2(val context: Context, val appParameters: AppParameters) {

    private val rnd : Random = Random()
    val userCryptogram : Cryptogram = Cryptogram()
    val receiverCryptogram : Cryptogram = Cryptogram()
    lateinit var cryptoCore : CryptoCore

    private val SERVICE_UUID = UUID.fromString("18b41747-01df-44d1-bc25-187082eb76bf")
    private val NONCE_USER_CHAR_UUID = UUID.fromString("bc3ba145-f588-4f86-8bc4-fb925a23dc31")
    private val NONCE_RECEIVER_CHAR_UUID = UUID.fromString("c8ba0ef6-5c27-11ed-9b6a-0242ac120002")
    private val IDR_CHAR_UUID = UUID.fromString("01e6ee2d-420a-4380-8e14-2a83844d4ae8")
    private val HATU_CHAR_UUID = UUID.fromString("b828f7a3-157a-4a4e-9c9b-3feb19b8e90d")
    private val IV_USER_CHAR_UUID = UUID.fromString("27938afb-f6f0-4e19-a4b2-2545da6bad40")
    private val CRYPTOGRAM_USER_CHAR_UUID = UUID.fromString("23cab78f-28d9-4ecb-bfd1-1bba7b486fa3")
    var cryptogramCounter = 2

  //  private val nonceUserCharacteristic : BluetoothGattCharacteristic =
  //      BluetoothGattCharacteristic(
  //          UUID.fromString("bc3ba145-f588-4f86-8bc4-fb925a23dc31"),
  //          BluetoothGattCharacteristic.PROPERTY_WRITE + BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE)
//
  //  private val nonceReceiverCharacteristic : BluetoothGattCharacteristic =
  //      BluetoothGattCharacteristic(
  //          UUID.fromString("c8ba0ef6-5c27-11ed-9b6a-0242ac120002"),
  //          BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ)
//
  //  private val idrCharacteristic : BluetoothGattCharacteristic =
  //      BluetoothGattCharacteristic(
  //          UUID.fromString("01e6ee2d-420a-4380-8e14-2a83844d4ae8"),
  //          BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ)
//
  //  private val hatuCharacteristic : BluetoothGattCharacteristic =
  //      BluetoothGattCharacteristic(
  //          UUID.fromString("b828f7a3-157a-4a4e-9c9b-3feb19b8e90d"),
  //          BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE )
//
  //  private val ivCharacteristic : BluetoothGattCharacteristic =
  //      BluetoothGattCharacteristic(
  //          UUID.fromString("27938afb-f6f0-4e19-a4b2-2545da6bad40"),
  //          BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE )
//
  //  private val cryptogramCharacteristic : BluetoothGattCharacteristic =
  //      BluetoothGattCharacteristic(
  //          UUID.fromString("23cab78f-28d9-4ecb-bfd1-1bba7b486fa3"),
  //          BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE )

    val peripheralCallback : BluetoothPeripheralCallback =
        object : BluetoothPeripheralCallback() {
            override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {
                Log.i("onServiceDiscovered",peripheral.toString())
                super.onServicesDiscovered(peripheral)
                //peripheral.readCharacteristic(UUID.fromString("18b41747-01df-44d1-bc25-187082eb76bf"),
                //    UUID.fromString("bc3ba145-f588-4f86-8bc4-fb925a23dc31"))
                //peripheral.readCharacteristic(bluetoothCharacteristic) //DALŠÍ MOŽNOST ČTENÍ
                val nonceReceiverCharacteristic = peripheral.getCharacteristic(SERVICE_UUID,NONCE_RECEIVER_CHAR_UUID)
                val nonceUserCharacteristic = peripheral.getCharacteristic(SERVICE_UUID,NONCE_USER_CHAR_UUID)
                val idrCharacteristic : BluetoothGattCharacteristic? = peripheral.getCharacteristic(SERVICE_UUID,IDR_CHAR_UUID)
                val hatuCharacteristic = peripheral.getCharacteristic(SERVICE_UUID, HATU_CHAR_UUID)

                if(nonceReceiverCharacteristic != null)
                {
                    peripheral.setNotify(nonceReceiverCharacteristic,true)
                    peripheral.readCharacteristic(nonceReceiverCharacteristic)
                }
                if(nonceUserCharacteristic != null)
                {
                    peripheral.setNotify(nonceUserCharacteristic,true)
                    val value = rnd.nextInt()
                    val bluetoothBytesParser : BluetoothBytesParser = BluetoothBytesParser()
                    bluetoothBytesParser.setString(value.toString())
                    peripheral.writeCharacteristic(nonceUserCharacteristic,bluetoothBytesParser.value,WriteType.WITH_RESPONSE)
                    userCryptogram.nonce = value
                }
                if (idrCharacteristic != null) {
                    peripheral.setNotify(idrCharacteristic,true)
                    peripheral.readCharacteristic(idrCharacteristic)
                }
                if (hatuCharacteristic != null) {
                    peripheral.setNotify(hatuCharacteristic, true)
                    val bluetoothBytesParser : BluetoothBytesParser = BluetoothBytesParser()
                    bluetoothBytesParser.setString(appParameters.hatu)
                    peripheral.writeCharacteristic(hatuCharacteristic,bluetoothBytesParser.value,WriteType.WITH_RESPONSE)
                }

            }

            override fun onCharacteristicUpdate(
                peripheral: BluetoothPeripheral,
                value: ByteArray?,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                super.onCharacteristicUpdate(peripheral, value, characteristic, status)
                val characteristicUUID = characteristic.uuid
                val parser = BluetoothBytesParser(value)
                Log.i("onCharacteristicUpdate", parser.getStringValue(0)) //825241648
                if (characteristicUUID.equals(NONCE_RECEIVER_CHAR_UUID)){
                    Log.i("NonceRChar","přečteno")
                    val nrString = parser.getStringValue(0)
                    receiverCryptogram.nonce = Integer.parseInt(nrString)
                }
                else if(characteristicUUID.equals(IDR_CHAR_UUID)){
                    Log.i("IdrChar","přečteno")
                    val idrString = parser.getStringValue(0)
                    receiverCryptogram.idr = Integer.parseInt(idrString)
                    val ivCharacteristic = peripheral.getCharacteristic(SERVICE_UUID, IV_USER_CHAR_UUID)
                    val cryptogramCharacteristic = peripheral.getCharacteristic(SERVICE_UUID, CRYPTOGRAM_USER_CHAR_UUID)
                    if (cryptogramCharacteristic != null){
                        cryptoCore = CryptoCore(appParameters,userCryptogram,receiverCryptogram)
                        cryptoCore.setUserIv()
                        peripheral.setNotify(cryptogramCharacteristic, true)
                        val bluetoothBytesParser : BluetoothBytesParser = BluetoothBytesParser()
                        bluetoothBytesParser.setString(cryptoCore.getFinalCipher())
                        peripheral.writeCharacteristic(cryptogramCharacteristic,bluetoothBytesParser.value,WriteType.WITH_RESPONSE)
                        cryptogramCounter = 4
                    }
                    if (ivCharacteristic != null){
                        cryptoCore = CryptoCore(appParameters,userCryptogram,receiverCryptogram)
                        peripheral.setNotify(ivCharacteristic, true)
                        peripheral.writeCharacteristic(ivCharacteristic,cryptoCore.getUserIv(),WriteType.WITH_RESPONSE)
                    }
                }

                if (cryptogramCounter == 4){
                    val auth = parser.getStringValue(0)
                    Log.i("C2:",parser.getStringValue(0))
                    if(auth.equals("true")){
                        val intentBle = Intent(context, AuthActivityBle::class.java)
                        context.startActivity(intentBle)
                    }
                    else {
                        val intentBle = Intent(context, NauthActivityBle::class.java)
                        context.startActivity(intentBle)

                    }

                }/*
                if (characteristicUUID.equals(CRYPTOGRAM_USER_CHAR_UUID)){
                    cryptoCore = CryptoCore(appParameters,userCryptogram,receiverCryptogram)
                    if (cryptogramCounter == 2){
                        Log.i("C2:",parser.getStringValue(0))
                        val cryptogramCharacteristic = peripheral.getCharacteristic(SERVICE_UUID, CRYPTOGRAM_USER_CHAR_UUID)
                        //if (cryptogramCharacteristic != null) {
                        //    peripheral.setNotify(cryptogramCharacteristic, true)
                        //}
                        if (cryptogramCharacteristic != null) {
                            val bluetoothBytesParser : BluetoothBytesParser = BluetoothBytesParser()
                            bluetoothBytesParser.setString(cryptoCore.getFinalCipher())
                            peripheral.writeCharacteristic(cryptogramCharacteristic,bluetoothBytesParser.value,WriteType.WITH_RESPONSE)
                            Log.i("C2:","zapisuju")
                        }
                        cryptogramCounter = 4
                    }
                }*/
            }

            override fun onCharacteristicWrite(
                peripheral: BluetoothPeripheral,
                value: ByteArray?,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                val parser = BluetoothBytesParser(value)
                val characteristicUUID = characteristic.uuid
                if (status == GattStatus.SUCCESS) {
                    Log.i("SUCCESS: Writing " + parser.getStringValue(0), "to " +  characteristic.getUuid());
                } else {
                    Log.i("ERROR: Failed writing " + parser.getStringValue(0), "to " +  characteristic.getUuid());
                }
                if (characteristicUUID.equals(CRYPTOGRAM_USER_CHAR_UUID)){
                    Log.i("cryptogram", parser.getStringValue(0))
                }
                if (characteristicUUID.equals(IV_USER_CHAR_UUID)){
                    Log.i("IV: ", Arrays.toString(parser.value))
                }
            }

            override fun onNotificationStateUpdate(
                peripheral: BluetoothPeripheral,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                if (status == GattStatus.SUCCESS) {
                    if (peripheral.isNotifying(characteristic)) {
                        Log.i(
                            "",
                            String.format("SUCCESS: Notify set to 'on' for %s", characteristic.uuid)
                        )
                    } else {
                        Log.i(
                            "",
                            String.format(
                                "SUCCESS: Notify set to 'off' for %s",
                                characteristic.uuid
                            )
                        )
                    }
                } else {
                    Log.e(
                        "",
                        String.format(
                            "ERROR: Changing notification state failed for %s",
                            characteristic.uuid
                        )
                    )
                }
            }

        }

    private val bluetoothCentralManagerCallback: BluetoothCentralManagerCallback =
        object : BluetoothCentralManagerCallback() {
            override fun onDiscoveredPeripheral(
                peripheral: BluetoothPeripheral,
                scanResult: ScanResult
            ) {
                Log.i("onDiscoveredPeripheral","zdařilo sa")
                central.stopScan()
                central.connectPeripheral(peripheral, peripheralCallback)
            }

            override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
                super.onConnectionFailed(peripheral, status)
                Log.i("nezdařilo sa","nezdařilo sa")
            }

            override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
                super.onConnectedPeripheral(peripheral)
                Log.i("onConnectedPeripheral", "zdařilo sa")
            }
        }

    val mainActivity : MainActivity = MainActivity()


    var central = BluetoothCentralManager(
        context,
        bluetoothCentralManagerCallback,
        Handler(Looper.getMainLooper())
    )

    var peripheral: BluetoothPeripheral = central.getPeripheral("08:00:27:6F:9D:46") //08:00:27:9E:84:4C
    //var peripheral: BluetoothPeripheral = central.getPeripheral("00:E0:4C:6C:20:03") //08:00:27:9E:84:4C

    //08:00:27:F6:B2:E1


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
}