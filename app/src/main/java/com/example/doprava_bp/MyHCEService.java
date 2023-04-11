package com.example.doprava_bp;

import static android.nfc.NfcAdapter.EXTRA_TAG;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.cardemulation.HostApduService;
import android.nfc.tech.IsoDep;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class MyHCEService extends HostApduService implements IHCEBinder {
    private static final String TAG = "MyHCEService";
    private static final byte[] SELECT_APDU_APP = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
            (byte) 0x07,
            (byte) 0xF0, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06,
            (byte) 0x00};
    private static final byte[] UNKNOWN_COMMAND = {(byte) 0x6D, (byte) 0x00};
    private static final String AID = "F0010203040506";
    private Messenger mMessenger;
    private PendingIntent mPendingIntent;
    private final IBinder binder = new LocalBinder();
    private IBinder mBinder = new LocalBinder();
    private AppParameters appParameters = new AppParameters();
    private Cryptogram userCryptogram;
    private Cryptogram receiverCryptogram;
    private CryptoCore cryptoCore;

    // AID for our loyalty card service.
    private static final String SAMPLE_LOYALTY_CARD_AID = "F222222222";

    // ISO-DEP command HEADER for selecting an AID.
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    private static final String SELECT_APDU_HEADER = "00A40400";

    // ISO-DEP command for selecting our loyalty card service.
    //private static final String SELECT_APDU_LOYALTY = SELECT_APDU_HEADER + String.format("%02X", SAMPLE_LOYALTY_CARD_AID.length() / 2) + SAMPLE_LOYALTY_CARD_AID;

    // Response APDU indicating successful processing of a command.
    private static final byte[] SELECT_OK_SW = hexStringToByteArray("9000");
    private Intent activityIntent;
    private static MyHCEService instance;

    public static MyHCEService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        activityIntent = new Intent(this, MainActivity.class);
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.d(TAG, "processCommandApdu: " + bytesToHex(commandApdu));
        byte[] responseApdu;
        byte[] commandHeader = new byte[4];
        System.arraycopy(commandApdu, 0, commandHeader, 0, 4);
        if (Arrays.equals(commandApdu, SELECT_APDU_APP)) {
            Log.d(TAG, "Application selected");
            byte[] responseData = "Response from HCE service".getBytes();
            return concatenateArrays(responseData, SELECT_OK_SW);
        }
        else if (Arrays.equals(commandHeader, new byte[]{0x00, 0x01, 0x02, 0x03})) {
            Log.d(TAG,"zacatek protokolu pres NFC");
            userCryptogram = new Cryptogram();
            userCryptogram.setHatu(appParameters.getHatu());
            userCryptogram.setNonce(2000);
            Log.i("Hatu string:",userCryptogram.getHatu());
            //Log.i("Hatu string:",Arrays.toString(userCryptogram.getHatu().getBytes()));
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(userCryptogram.getNonce());
            byte[] byteArray = buffer.array();
            byte [] responseData = concatenateArrays(userCryptogram.getHatu().getBytes(),byteArray);
            //Log.i("posílá se:",Arrays.toString(responseData));
            receiverCryptogram = new Cryptogram();
            byte[] nonceArray = new byte[4];
            byte[] idrArray = new byte[4];
            System.arraycopy(commandApdu,4,nonceArray,0,4);
            System.arraycopy(commandApdu,8,idrArray,0,4);
            receiverCryptogram.setNonce(ByteBuffer.wrap(nonceArray).getInt());
            receiverCryptogram.setIdr(ByteBuffer.wrap(idrArray).getInt());
            Log.i(String.valueOf(receiverCryptogram.getIdr()),String.valueOf(receiverCryptogram.getNonce()));
            return concatenateArrays(responseData, SELECT_OK_SW);
        }
        else if (Arrays.equals(commandHeader, new byte[]{0x00, 0x01, 0x02, 0x04})) {
            cryptoCore = new CryptoCore(appParameters,userCryptogram,receiverCryptogram);
            cryptoCore.setUserIv();
            userCryptogram.cryptograms.add(cryptoCore.getFinalCipher());
            byte [] cipher = decodeHexString(userCryptogram.cryptograms.get(0));
            byte [] responseData = concatenateArrays(cipher,cryptoCore.getUserIv());
            Log.i("posílá se:",Arrays.toString(responseData));
            return concatenateArrays(responseData, SELECT_OK_SW);
        }
        else if (Arrays.equals(commandHeader, new byte[]{0x00, 0x01, 0x02, 0x05})){
            Log.i("autentizovan","auth");
            Intent intent = new Intent(MyHCEService.this, AuthActivity.class);
            startActivity(intent);
            return SELECT_OK_SW;
        }
        else if (Arrays.equals(commandHeader, new byte[]{0x00, 0x01, 0x02, 0x06})){
            Log.i("neautentizovan","auth");
            return SELECT_OK_SW;
        }
        else {
            responseApdu = UNKNOWN_COMMAND;
        }
        return responseApdu;
    }

    //@Override
    //public int onStartCommand(Intent intent, int flags, int startId) {
    //    activityIntent = intent.getParcelableExtra("intent");
    //    // Retrieve the PendingIntent and store it as a class variable
    //    mPendingIntent = intent.getParcelableExtra(EXTRA_TAG);
    //    return START_STICKY;
    //}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("Hatu")) {
            appParameters.setUserKey(intent.getStringExtra("UserKey"));
            appParameters.setHatu(intent.getStringExtra("Hatu"));
            appParameters.setKeyLengths(intent.getIntExtra("KeyLength",192));
            appParameters.setATU(intent.getByteArrayExtra("Atu"));
            Log.d(TAG, "Received data from MainActivity");
        }
        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "onDeactivated:" + reason);
    }

    public String bytesToHex(byte[] bytes) {

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public IBinder onHCEBind() {
        return mBinder;
    }


    public class LocalBinder extends Binder {
        MyHCEService getService() {
            // Return the instance of MyHCEService for clients to call public methods on
            return MyHCEService.this;
        }
    }

    public byte[] decodeHexString(String hexString) {
        if (hexString.length() % 2 == 1) {
            throw new IllegalArgumentException(
                    "Invalid hexadecimal String supplied.");
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
        }
        return bytes;
    }

    public byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    private int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if(digit == -1) {
            throw new IllegalArgumentException(
                    "Invalid Hexadecimal Character: "+ hexChar);
        }
        return digit;
    }


    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return result;
    }

    private byte[] concatenateArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

   // public void transmit(byte[] data) {
   //     if (mPendingIntent != null) {
   //         try {
   //             mPendingIntent.send(this, 0, new Intent().putExtra("data", data));
   //         } catch (PendingIntent.CanceledException e) {
   //             Log.e(TAG, "Error transmitting data", e);
   //         }
   //     }
   // }

    public void transmit(Tag tag, byte[] data) {
        Log.i("HCE Transmit:",bytesToHex(data));
        IsoDep isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();
            byte[] response = isoDep.transceive(data);
            Log.d(TAG, "Response: " + bytesToHex(response));
        } catch (IOException e) {
            Log.e(TAG, "Error transmitting data", e);
        } finally {
            try {
                isoDep.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing IsoDep", e);
            }
        }
    }

    /*
     public void transmit(byte[] data) {
        IsoDep isoDep = IsoDep.get(getTag());
        try {
            isoDep.connect();
            byte[] response = isoDep.transceive(data);
            Log.d(TAG, "Response: " + bytesToHex(response));
            isoDep.close();
        } catch (IOException e) {
            Log.e(TAG, "Error transmitting data", e);
        }
    }

    public void transmit(byte[] data) {
        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            IsoDep isoDep = IsoDep.get(tag);
            try {
                isoDep.connect();
                byte[] response = isoDep.transceive(data);
                Log.d(TAG, "Response: " + HexUtils.bytesToHex(response));
                isoDep.close();
            } catch (IOException e) {
                Log.e(TAG, "Error transmitting data", e);
            }
        }
    }
     */


}
