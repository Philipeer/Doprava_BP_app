package com.example.doprava_bp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cryptogram implements Serializable {
    private int nonce;
    private int idr;
    private byte[] ATU;
    private String hatu;
    public List<String> cryptograms = new ArrayList<>();

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public int getIdr() {
        return idr;
    }

    public void setIdr(int idr) {
        this.idr = idr;
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
}
