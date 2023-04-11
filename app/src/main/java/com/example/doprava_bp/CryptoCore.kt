package com.example.doprava_bp

import android.util.Log
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoCore(val appParameters: AppParameters, val userCryptogram: Cryptogram,
                 val receiverCryptogram: Cryptogram) {

    val GCM_IV_LENGTH = 12
   // private var ukeyString = hash(appParameters.userKey + "user" + userCryptogram.nonce.toString() +
   //                                     receiverCryptogram.nonce.toString(), "SHA-1")
   // private var plaintextString = appParameters.hatu + receiverCryptogram.idr + userCryptogram.nonce.toString() +
   //         receiverCryptogram.nonce.toString()

    fun getCipherText(): String {
        val ukeyContent = appParameters.userKey + "user" + userCryptogram.nonce.toString() +
                receiverCryptogram.nonce.toString()
        val plaintextContent = appParameters.hatu + receiverCryptogram.idr + userCryptogram.nonce.toString() +
                receiverCryptogram.nonce.toString()
        Log.i(ukeyContent,plaintextContent)
        var hashType:String = "SHA-1"
        if (appParameters.keyLengths == 128){
            hashType = "SHA-1"
        }
        else if (appParameters.keyLengths == 192){
            hashType = "SHA-224"
        }
        else if (appParameters.keyLengths == 256){
            hashType = "SHA-256"
        }
        val userKey = appParameters.userKey.substring(0,(appParameters.keyLengths/8))
        var ukeyString = hash(userKey + "user" + userCryptogram.nonce.toString() +
                receiverCryptogram.nonce.toString(), hashType)
        var plaintextString = appParameters.hatu + receiverCryptogram.idr + userCryptogram.nonce.toString() +
                receiverCryptogram.nonce.toString()
        var plaintext: ByteArray = plaintextString.toByteArray()
        Log.i("Plaintext: ", Arrays.toString(plaintext))
        if (ukeyString != null) {
            ukeyString = ukeyString!!.substring(0,(appParameters.keyLengths/8))
        }
        if (ukeyString != null) {
            Log.i("ukeyString:",ukeyString)
        }
        val ukey: SecretKey = SecretKeySpec(ukeyString!!.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        var iv = ByteArray(GCM_IV_LENGTH)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)
        iv = convertToPositiveBytes(iv)
        val parameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, ukey, parameterSpec)
        var ciphertext: ByteArray = cipher.doFinal(plaintext)
        userCryptogram.iv = iv
        Log.i("IV: ", Arrays.toString(iv))
        //ciphertext = convertToPositiveBytes(ciphertext)
        val ciphertextHex = ciphertext.toHex()
        return ciphertextHex
    }

    fun getUserIv() : ByteArray{
        return userCryptogram.iv
    }

    fun setUserIv(){
        var iv = ByteArray(GCM_IV_LENGTH)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)
        iv = convertToPositiveBytes(iv)
        userCryptogram.iv = iv
    }

    fun getFinalCipher(): String {
        var hashType:String = "SHA-1"
        if (appParameters.keyLengths == 128){
            hashType = "SHA-1"
        }
        else if (appParameters.keyLengths == 192){
            hashType = "SHA-224"
        }
        else if (appParameters.keyLengths == 256){
            hashType = "SHA-256"
        }
        val userKey = appParameters.userKey.substring(0,(appParameters.keyLengths/8))
        var ukeyString = hash(userKey + "user" + userCryptogram.nonce.toString() +
                receiverCryptogram.nonce.toString(), hashType)
        if (ukeyString != null) {
            ukeyString = ukeyString!!.substring(0,(appParameters.keyLengths/8))
        }
        Log.i("ukeyString content: ",userKey + "user" + userCryptogram.nonce.toString() +
                receiverCryptogram.nonce.toString())
        val command = "unlock"
        val ukey: SecretKey = SecretKeySpec(ukeyString!!.toByteArray(), "AES")
        val parameterSpec = GCMParameterSpec(128, getUserIv())
        val cipherC3 = Cipher.getInstance("AES/GCM/NoPadding")
        cipherC3.init(Cipher.ENCRYPT_MODE, ukey, parameterSpec)
        val plaintext = appParameters.atu + command.toByteArray()
        var ciphertext = cipherC3.doFinal(plaintext)
        //Log.i("getFinalCipher:", Arrays.toString(ciphertext))
        //ciphertext = convertToPositiveBytes(ciphertext)
        Log.i("IV array: ", Arrays.toString(getUserIv()))
        Log.i("ukey array: ", Arrays.toString(ukey.encoded))
        Log.i("C2 array: ", Arrays.toString(ciphertext))
        val ciphertextHex = ciphertext.toHex()
        return ciphertextHex
        //return ciphertext //Tohle lze vracet až bude vyřešen problém s přenášení negativních bajtů
    }


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

    fun convertToPositiveBytes(bytes: ByteArray): ByteArray {
        val byteArray = ByteArray(bytes.size)
        for (i in bytes.indices) {
            byteArray[i] = byteAbs(bytes[i])
        }
        return byteArray
    }

    fun byteAbs(b: Byte): Byte {
        var b = b
        return if (b.toInt() < 0) {
            b = Math.abs(b.toInt()).toByte()
            b
        } else b
    }

    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
}