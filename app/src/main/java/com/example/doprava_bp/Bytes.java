package com.example.doprava_bp;

public class Bytes {
    public byte[] convertToPositiveBytes(byte[] bytes){
        byte[] byteArray = new byte[bytes.length];
        for(int i=0;i<bytes.length;i++){
            byteArray[i] = byteAbs(bytes[i]);
        }
        return byteArray;
    }

    byte byteAbs(byte b) {
        return b >= 0? b : (byte) (b == -128 ? 127 : -b);
    }
}
