package com.handsriver.concierge.utilities;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Created by alain_r._trouve_silva after 04-03-17.
 */

public class Utility {

    public static String changeDateFormat (String beforeDate, String format){
        String afterDate = null;
        SimpleDateFormat oldPattern, newPattern;
        ParsePosition position = new ParsePosition(0);

        if (format.equals("BIRTHDATE"))
        {
            oldPattern = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
            Date date1 = oldPattern.parse(beforeDate,position);
            newPattern = new SimpleDateFormat("dd-MM-yyyy");
            afterDate = newPattern.format(date1);
        }

        if (format.equals("ENTRY"))
        {
            oldPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
            Date date1 = oldPattern.parse(beforeDate,position);
            newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",Locale.US);
            afterDate = newPattern.format(date1);
        }

        return afterDate;
    }

    public static String changeDateFormatDatabase (String beforeDate){
        String afterDate;
        SimpleDateFormat oldPattern, newPattern;
        ParsePosition position = new ParsePosition(0);

        oldPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",Locale.US);
        Date date1 = oldPattern.parse(beforeDate,position);
        newPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
        afterDate = newPattern.format(date1);

        return afterDate;
    }

    public static void hide_keyboard(Activity activity, IBinder iBinder){
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(iBinder, 0);
    }

    public static boolean differenceDateHours (String exitPorter, String entryPorter, int hours){
        ParsePosition position = new ParsePosition(0);
        ParsePosition position2 = new ParsePosition(0);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
        Date dateEntry = simpleDateFormat.parse(entryPorter,position);
        Date dateExit = simpleDateFormat.parse(exitPorter,position2);

        long different =  dateExit.getTime() - dateEntry.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long minuteslimit = hours * 60;

        long elapsedMinutes = different / minutesInMilli;

        if((elapsedMinutes >= minuteslimit) && (hours != 0)){
            return true;
        }
        else{
            return false;
        }
    }

    public static boolean differenceDateHoursMinutesExtra (String exitPorter, String entryPorter, int hours, int minutes){
        ParsePosition position = new ParsePosition(0);
        ParsePosition position2 = new ParsePosition(0);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
        Date dateEntry = simpleDateFormat.parse(entryPorter,position);
        Date dateExit = simpleDateFormat.parse(exitPorter,position2);

        long different =  dateExit.getTime() - dateEntry.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long minuteslimit = hours * 60 - minutes;

        long elapsedMinutes = different / minutesInMilli;

        if((elapsedMinutes >= minuteslimit) && (hours != 0)){
            return true;
        }
        else{
            return false;
        }
    }

    public static String getHourForServer(){
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        return df.format(calendar.getTime());
    }

    public static String getHourForServerOnlyDate(){
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        return df.format(calendar.getTime());
    }

    public static String getDateSimpleForServer7Days(){
        //en vez de 7 dias seran un año correspondiente a 365
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        calendar.add(Calendar.DAY_OF_MONTH, -90);
        return df.format(calendar.getTime());
    }

    public static String getDateSimpleForServer365Days(){
        //365, solo para los pagos
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        calendar.add(Calendar.DAY_OF_MONTH, -365);
        return df.format(calendar.getTime());
    }



    public static String getDateSimpleForServer1Days(){
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return df.format(calendar.getTime());
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isScannerOCR(View v, int keycode, KeyEvent event){
        if (event.getDeviceId() == KeyCharacterMap.VIRTUAL_KEYBOARD) {
            return false;
        }
        return true;
    }

    public static void hideKeyboard(View view,Context context) {
        InputMethodManager im =(InputMethodManager)context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String generateHash(String text){
        String hash;
        try{
            MessageDigest message = MessageDigest.getInstance("SHA256");
            message.reset();
            message.update(text.getBytes());
            byte[] b = message.digest();
            hash = Base64.encodeToString(b,Base64.NO_WRAP);

        }catch (NoSuchAlgorithmException e){
            return "";
        }
        return hash;

    }

    public static byte[] object2Bytes( Object o ){
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream( baos );
            oos.writeObject( o );
            return baos.toByteArray();

        }
        catch (IOException e){

            return null;
        }


    }

    public static Object bytes2Object( byte raw[] )
            throws IOException, ClassNotFoundException {

        try{
            ByteArrayInputStream bais = new ByteArrayInputStream( raw );
            ObjectInputStream ois = new ObjectInputStream( bais );
            Object o = ois.readObject();
            return o;

        }
        catch (IOException e){

            return null;
        }



    }




/*
    public static String encrypt(String plaintext) {

        byte[] KEY_VALUE = "v-BasduHd(9#Hd&C>p)j&/J/bn38nJ3#".getBytes();

        try{
            Key key = new SecretKeySpec(KEY_VALUE, "AES");
            String serializedPlaintext = "s:" + plaintext.getBytes().length + ":\"" + plaintext + "\";";
            byte[] plaintextBytes = serializedPlaintext.getBytes("UTF-8");

            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = c.getIV();
            byte[] encVal = c.doFinal(plaintextBytes);
            String encryptedData = Base64.encodeToString(encVal, Base64.NO_WRAP);

            SecretKeySpec macKey = new SecretKeySpec(KEY_VALUE, "HmacSHA256");
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            hmacSha256.init(macKey);
            hmacSha256.update(Base64.encode(iv, Base64.NO_WRAP));
            byte[] calcMac = hmacSha256.doFinal(encryptedData.getBytes("UTF-8"));
            String mac = new String(Hex.encodeHex(calcMac));

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("iv", Base64.encodeToString(iv, Base64.NO_WRAP));
            jsonObject.accumulate("value",encryptedData);
            jsonObject.accumulate("mac",mac);

            return Base64.encodeToString(jsonObject.toString().getBytes("UTF-8"), Base64.DEFAULT);
        }catch (Exception e){
            return null;
        }

    }
    */
}
