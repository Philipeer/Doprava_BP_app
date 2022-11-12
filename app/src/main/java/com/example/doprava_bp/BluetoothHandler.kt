package com.example.doprava_bp

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.welie.blessed.*
import java.util.*

class BluetoothHandler(val context: Context) {

    private val rnd : Random = Random()

    private val nonceUserCharacteristic : BluetoothGattCharacteristic =
        BluetoothGattCharacteristic(
            UUID.fromString("bc3ba145-f588-4f86-8bc4-fb925a23dc31"),
            BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE )

    private val nonceReceiverCharacteristic : BluetoothGattCharacteristic =
        BluetoothGattCharacteristic(
            UUID.fromString("c8ba0ef6-5c27-11ed-9b6a-0242ac120002"),
            BluetoothGattCharacteristic.PERMISSION_READ, BluetoothGattCharacteristic.PERMISSION_READ)

    private val idrCharacteristic : BluetoothGattCharacteristic =
        BluetoothGattCharacteristic(
            UUID.fromString("01e6ee2d-420a-4380-8e14-2a83844d4ae8"),
            BluetoothGattCharacteristic.PERMISSION_READ, BluetoothGattCharacteristic.PERMISSION_READ)

    private val hatuCharacteristic : BluetoothGattCharacteristic =
        BluetoothGattCharacteristic(
            UUID.fromString("b828f7a3-157a-4a4e-9c9b-3feb19b8e90d"),
            BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE )

    private val ivCharacteristic : BluetoothGattCharacteristic =
        BluetoothGattCharacteristic(
            UUID.fromString("27938afb-f6f0-4e19-a4b2-2545da6bad40"),
            BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE )

    private val cryptogramCharacteristic : BluetoothGattCharacteristic =
        BluetoothGattCharacteristic(
            UUID.fromString("23cab78f-28d9-4ecb-bfd1-1bba7b486fa3"),
            BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE )

    val peripheralCallback : BluetoothPeripheralCallback =
        object : BluetoothPeripheralCallback() {
            override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {
                super.onServicesDiscovered(peripheral)
                //peripheral.readCharacteristic(UUID.fromString("18b41747-01df-44d1-bc25-187082eb76bf"),
                //    UUID.fromString("bc3ba145-f588-4f86-8bc4-fb925a23dc31"))
                //peripheral.readCharacteristic(bluetoothCharacteristic) //DALŠÍ MOŽNOST ČTENÍ
                peripheral.writeCharacteristic(nonceUserCharacteristic, intToBytes(rnd.nextInt()) ,
                    WriteType.WITH_RESPONSE)
                peripheral.readCharacteristic(nonceReceiverCharacteristic)
                peripheral.readCharacteristic(idrCharacteristic)
                //peripheral.writeCharacteristic(hatuCharacteristic, client.appParameters.hatu.toByteArray(),
                //WriteType.WITH_RESPONSE)
            }

            override fun onCharacteristicUpdate(
                peripheral: BluetoothPeripheral,
                value: ByteArray?,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                super.onCharacteristicUpdate(peripheral, value, characteristic, status)
                if(characteristic == nonceReceiverCharacteristic){
                    if (status == GattStatus.SUCCESS){
                        //receiverCryptogram.nonce = convertByteArrayToInt(value)
                    }
                }
                if(characteristic == idrCharacteristic){
                    if (status == GattStatus.SUCCESS){
                        //receiverCryptogram.idr = convertByteArrayToInt(value)
                    }
                }
            }

            override fun onCharacteristicWrite(
                peripheral: BluetoothPeripheral,
                value: ByteArray?,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                super.onCharacteristicWrite(peripheral, value, characteristic, status)
                if (characteristic == nonceUserCharacteristic){
                    if (status == GattStatus.SUCCESS){
                        //userCryptogram.nonce = convertByteArrayToInt(value)
                    }
                }
            }

        }

    private val bluetoothCentralManagerCallback: BluetoothCentralManagerCallback =
        object : BluetoothCentralManagerCallback() {
            override fun onDiscoveredPeripheral(
                peripheral: BluetoothPeripheral,
                scanResult: ScanResult
            ) {
                central.stopScan()
                central.connectPeripheral(peripheral, peripheralCallback)
            }

            override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
                super.onConnectionFailed(peripheral, status)
                Log.i("nezdařilo sa","nezdařilo sa")
            }
        }

    val mainActivity : MainActivity = MainActivity()


    var central = BluetoothCentralManager(
        context,
        bluetoothCentralManagerCallback,
        Handler(Looper.getMainLooper())
    )


    var peripheral: BluetoothPeripheral = central.getPeripheral("88:44:77:28:AC:B6") //08:00:27:9E:84:4C
    //08:00:27:F6:B2:E1

    private fun intToBytes(data: Int): ByteArray? {
        return byteArrayOf(
            (data shr 24 and 0xff).toByte(),
            (data shr 16 and 0xff).toByte(),
            (data shr 8 and 0xff).toByte(),
            (data shr 0 and 0xff).toByte()
        )
    }
}