package com.example.doprava_bp;

import static android.nfc.NfcAdapter.EXTRA_TAG;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.cardemulation.HostApduService;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

public class MyHCEService extends HostApduService {
    private static final String TAG = "MyHCEService";
    private static final byte[] SELECT_APDU_APP = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
            (byte) 0x07,
            (byte) 0xF0, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06,
            (byte) 0x00};
    private static final byte[] UNKNOWN_COMMAND = {(byte) 0x6D, (byte) 0x00};
    private static final String AID = "F0010203040506";
    private Messenger mMessenger;
    private PendingIntent mPendingIntent;

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

    @Override
    public void onCreate() {
        super.onCreate();
        activityIntent = new Intent(this, MainActivity.class);
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.d(TAG, "processCommandApdu: " + bytesToHex(commandApdu));
        byte[] responseApdu;
        if (Arrays.equals(commandApdu, SELECT_APDU_APP)) {
            Log.d(TAG, "Application selected");
            byte[] responseData = "Response from HCE service".getBytes();
            return concatenateArrays(responseData, SELECT_OK_SW);
        } else {
            responseApdu = UNKNOWN_COMMAND;
        }
        return responseApdu;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        activityIntent = intent.getParcelableExtra("intent");
        // Retrieve the PendingIntent and store it as a class variable
        mPendingIntent = intent.getParcelableExtra(EXTRA_TAG);
        return START_STICKY;
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

    public void transmit(byte[] data) {
        IsoDep isoDep = IsoDep.get((Tag) null);
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
