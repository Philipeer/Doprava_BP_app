package com.example.doprava_bp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import com.example.doprava_bp.AppParameters;

import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

public class Client {
    private AppParameters appParameters;
    private Cryptogram userCryptogram;
    private Cryptogram receiverCryptogram;
    public static void main(String[] args) throws Exception {
        new Client();
    }

    public Client() throws Exception{

    }

    public void receiveParamsFromIdP() throws IOException, ClassNotFoundException {
        //Socket socket = new Socket("192.168.56.1", 10001);
        //Socket socket = new Socket("192.168.1.7", 10001); //BRNO
        //Socket socket = new Socket("192.168.145.1", 10001);
        //Socket socket = new Socket("192.168.126.1", 10001);
        //Socket socket = new Socket("192.168.99.1", 10001);
        //Socket socket = new Socket("10.0.0.30", 10001); //BV
        Socket socket = new Socket("10.0.0.135", 10001); //BV
        //Socket socket = new Socket("100.69.147.13", 10001); //vut
        //Socket socket = new Socket("172.20.10.6", 10001); //hotspot

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

        AppParameters helloMessage = new AppParameters("Hello from App!");
        objectOutputStream.writeObject(helloMessage);


        appParameters = (AppParameters) objectInputStream.readObject();
        /*
        System.out.println(appParameters.message);
        System.out.println(appParameters.getATU());
        System.out.println(appParameters.getHatu());
        System.out.println(appParameters.getUserKey());

         */

        objectOutputStream.close();
        socket.close();
    }

    public void sendAndReceiveObject() throws IOException, ClassNotFoundException {
        Socket socket = new Socket("192.168.56.1", 10002);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

        Random rnd = new Random();
        Cryptogram pokus = new Cryptogram();
        pokus.setNonce(rnd.nextInt());
        //userCryptogram.setNonce(rnd.nextInt());
        userCryptogram = pokus;
        objectOutputStream.writeObject(userCryptogram);


        receiverCryptogram = (Cryptogram) objectInputStream.readObject();

        objectOutputStream.close();
        socket.close();
    }

    public final void sendAndReceiveObject(Cryptogram userCryptogram, byte[] iv, byte[] ciphertext, int port) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("192.168.56.1", port);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

        userCryptogram.cryptograms.add(ciphertext);
        userCryptogram.setIv(iv);
        objectOutputStream.writeObject(userCryptogram);


        objectOutputStream.close();
        socket.close();
    }

    public AppParameters getAppParameters() {
        return appParameters;
    }

    public Cryptogram getUserCryptogram() {
        return userCryptogram;
    }

    public Cryptogram getReceiverCryptogram() {
        return receiverCryptogram;
    }

    public void amIAuthenticated() throws IOException, ClassNotFoundException {
        Socket socket = new Socket("192.168.56.1", 10004);
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        userCryptogram = (Cryptogram) objectInputStream.readObject();
        socket.close();
    }

    /*public void encrypt(@NotNull SecretKey ukey, @NotNull byte[] plaintext) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)
        val parameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, ukey, parameterSpec)
        var ciphertext: ByteArray = cipher.doFinal(plaintext)
    }*/
}
