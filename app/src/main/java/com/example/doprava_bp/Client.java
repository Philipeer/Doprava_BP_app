package com.example.doprava_bp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import com.example.doprava_bp.AppParameters;

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
        Socket socket = new Socket("192.168.56.1", 10001);

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

    public AppParameters getAppParameters() {
        return appParameters;
    }

    public Cryptogram getUserCryptogram() {
        return userCryptogram;
    }

    public Cryptogram getReceiverCryptogram() {
        return receiverCryptogram;
    }
}
