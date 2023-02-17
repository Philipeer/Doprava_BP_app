package com.example.doprava_bp;

import java.io.Serializable;

public class AppParameters implements Serializable {
    public String message;
    private String userKey;
    private byte[] ATU;
    private String hatu;
    private int keyLengths;

    public AppParameters() {
    }
    public AppParameters(String message) {
        this.message = message;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public byte[] getATU() {
        return ATU;
    }

    public void setATU(byte[] ATU) {
        this.ATU = ATU;
    }

    public String getHatu() {
        return hatu;
    }

    public void setHatu(String hatu) {
        this.hatu = hatu;
    }

    public int getKeyLengths() {
        return keyLengths;
    }

    public void setKeyLengths(int keyLengths) {
        this.keyLengths = keyLengths;
    }
}
