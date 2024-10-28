package com.handsriver.concierge.isapi.terminalfacial;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.ResidentVehicleEntry;
import com.handsriver.concierge.database.ConciergeContract.WhitelistEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.residents.Resident;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 27-04-17.
 */

public class ResidentsFaceSyncIsapi {

    public final String LOG_TAG = ResidentsFaceSyncIsapi.class.getSimpleName();
    public static final String RESIDENTSVEHICLES = "residentsvehicles";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String USER_CAMERA = "admin";
    public static final String PASS_CAMERA = "Citofon123";

    public static final String IS_ACTIVE = "1";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;


    public ResidentsFaceSyncIsapi(Context context) {
        this.mContext= context;
    }



    public static void saveBase64ToFile(String base64Data, String fileName) {
        // Decodificar la cadena base64 a bytes
        byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);

        // Obtener la ruta del directorio público DCIM
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());

        // Verificar si el directorio existe, si no, crearlo
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Crear el archivo en la ruta especificada
        File file = new File(dir, fileName);

        try {
            // Escribir los bytes decodificados en el archivo
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(decodedBytes);
            fos.close();

            System.out.println("Archivo creado exitosamente: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void addResident(Resident resident) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String IP_TERMINAL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_ips_facialterminal_key),"");

            String[] arrIpCameras = IP_TERMINAL.split(";", -1);

            for (String IP_CAMERA : arrIpCameras){

                if(!IP_CAMERA.equals("")){

                    //LA IMAGEN DEBE SER DESCARGADA DESDE EL S3

                    String s3_file = "fasfsafsafsafsasfa";
                    String file_name = "";
                    file_name =resident.getId()+".jpg";
                    saveBase64ToFile(s3_file,file_name);


                    //SE DEBEN PONER LOS DATOS DEL JSON
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("PIDI",resident.getId());



                    String auth = USER_CAMERA+":"+PASS_CAMERA;
                    byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.UTF_8),0);
                    String authHeaderValue = "Basic " + new String(encodedAuth);

                    URL url = new URL("http://"+IP_CAMERA+":80/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko");
                    urlConnection.setRequestProperty("Content-Type","text/plain");
                    urlConnection.setRequestProperty("Authorization", authHeaderValue);
                    urlConnection.setConnectTimeout(20000);
                    urlConnection.setReadTimeout(20000);
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    File dir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
                    File file = new File(dir, file_name);


                    OutputStream dataOutputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    OutputStreamWriter osw = new OutputStreamWriter(dataOutputStream, "UTF-8");
                    osw.write(jsonObject.toString());
                    osw.flush();
                    osw.close();

                    BufferedOutputStream bos = new BufferedOutputStream(urlConnection.getOutputStream());
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

                    int j;
                    byte[] buffer = new byte[4096];
                    while ((j = bis.read(buffer)) > 0) {
                        bos.write(buffer, 0, j);
                    }

                    bis.close();
                    bos.close();
                    String out = (((HttpURLConnection) urlConnection).getResponseMessage());
                    String out2 = out;
                }

            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

        }
    }


    public void updateResident(Resident resident) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String IP_TERMINAL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_ips_facialterminal_key),"");

            String[] arrIpCameras = IP_TERMINAL.split(";", -1);

            for (String IP_CAMERA : arrIpCameras){

                if(!IP_CAMERA.equals("")){

                    String file_name = "";
                    file_name =resident.getId()+".jpg";



                    //SE DEBEN PONER LOS DATOS DEL JSON
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("PIDI",resident.getId());



                    String auth = USER_CAMERA+":"+PASS_CAMERA;
                    byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.UTF_8),0);
                    String authHeaderValue = "Basic " + new String(encodedAuth);

                    URL url = new URL("http://"+IP_CAMERA+":80/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("PUT");
                    urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko");
                    urlConnection.setRequestProperty("Content-Type","text/plain");
                    urlConnection.setRequestProperty("Authorization", authHeaderValue);
                    urlConnection.setConnectTimeout(20000);
                    urlConnection.setReadTimeout(20000);
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    File dir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
                    File file = new File(dir, file_name);


                    OutputStream dataOutputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    OutputStreamWriter osw = new OutputStreamWriter(dataOutputStream, "UTF-8");
                    osw.write(jsonObject.toString());
                    osw.flush();
                    osw.close();

                    BufferedOutputStream bos = new BufferedOutputStream(urlConnection.getOutputStream());
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

                    int j;
                    byte[] buffer = new byte[4096];
                    while ((j = bis.read(buffer)) > 0) {
                        bos.write(buffer, 0, j);
                    }

                    bis.close();
                    bos.close();
                    String out = (((HttpURLConnection) urlConnection).getResponseMessage());
                    String out2 = out;
                }

            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

        }
    }

    public void deleteResident(Resident resident) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String IP_TERMINAL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_ips_facialterminal_key),"");

            String[] arrIpCameras = IP_TERMINAL.split(";", -1);

            for (String IP_CAMERA : arrIpCameras){

                if(!IP_CAMERA.equals("")){


                    //SE DEBEN PONER LOS DATOS DEL JSON
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("PIDI",resident.getId());



                    String auth = USER_CAMERA+":"+PASS_CAMERA;
                    byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.UTF_8),0);
                    String authHeaderValue = "Basic " + new String(encodedAuth);

                    URL url = new URL("http://"+IP_CAMERA+":80/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("PUT");
                    urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko");
                    urlConnection.setRequestProperty("Content-Type","text/plain");
                    urlConnection.setRequestProperty("Authorization", authHeaderValue);
                    urlConnection.setConnectTimeout(20000);
                    urlConnection.setReadTimeout(20000);
                    urlConnection.setDoInput(true);
                    urlConnection.connect();


                    OutputStream dataOutputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    OutputStreamWriter osw = new OutputStreamWriter(dataOutputStream, "UTF-8");
                    osw.write(jsonObject.toString());
                    osw.flush();
                    osw.close();


                    String out = (((HttpURLConnection) urlConnection).getResponseMessage());
                    String out2 = out;
                }

            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

        }
    }


}
