package com.handsriver.concierge.isapi.alpr;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
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
import com.handsriver.concierge.database.InsertUpdateTables.IUResidentsVehicles;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.residents.ResidentVehicle;
import com.handsriver.concierge.residents.ResidentVehicleAdapterSearchList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import org.apache.poi.hssf.usermodel.*;
import java.io.FileOutputStream;
/**
 * Created by Created by alain_r._trouve_silva after 27-04-17.
 */

public class ResidentsVehiclesSyncIsapi {

    public final String LOG_TAG = ResidentsVehiclesSyncIsapi.class.getSimpleName();
    public static final String RESIDENTSVEHICLES = "residentsvehicles";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String USER_CAMERA = "admin";
    public static final String PASS_CAMERA = "Citofonanpr#";

    public static final String IS_ACTIVE = "1";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;

    public static String plate = "";
    public static ArrayList<String> plates = new ArrayList<String>();


    public ResidentsVehiclesSyncIsapi(Context context) {
        this.mContext= context;
    }

    public static void generates_plates_alternatives(String plate, int index, int len ){

        if(plate.charAt(index)=='B'){
            int index_ = index;
            StringBuilder plate_aux = new StringBuilder(plate);
            plate_aux.setCharAt(index, '8');
            plates.add(plate_aux.toString());
            if(index>=len-1){
                return;
            }
            else{
                generates_plates_alternatives(plate_aux.toString(),index_+1,len);
            }
        }

        if(plate.charAt(index)=='8'){
            int index_ = index;
            StringBuilder plate_aux = new StringBuilder(plate);
            plate_aux.setCharAt(index, 'B');
            plates.add(plate_aux.toString());
            if(index>=len-1){
                return;
            }
            else{
                generates_plates_alternatives(plate_aux.toString(),index_+1,len);
            }
        }

        if(plate.charAt(index)=='D'){
            int index_ = index;
            StringBuilder plate_aux = new StringBuilder(plate);
            plate_aux.setCharAt(index, '0');
            plates.add(plate_aux.toString());
            if(index>=len-1){
                return;
            }
            else{
                generates_plates_alternatives(plate_aux.toString(),index_+1,len);
            }
        }
        if(index<len-1){
            generates_plates_alternatives(plate,index+1,len);
        }

        return;

    }

    @SuppressLint("Range")
    public void generate_file_v2(){
        dbHelper = new ConciergeDbHelper(mContext);
        db = DatabaseManager.getInstance().openDatabase();

        Log.d(LOG_TAG, "Starting sync ISAPI alpr");

        final String query = "SELECT " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry._ID + "," +
                ResidentVehicleEntry.TABLE_NAME + "." +ResidentVehicleEntry.COLUMN_PLATE + "," +
                ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + ResidentVehicleEntry.TABLE_NAME + "," + ConciergeContract.ApartmentEntry.TABLE_NAME +
                " WHERE " + ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry.COLUMN_APARTMENT_ID +
                " AND " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry.COLUMN_ACTIVE + " = " + IS_ACTIVE +

                " UNION SELECT " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry._ID + "," +
                WhitelistEntry.TABLE_NAME + "." +WhitelistEntry.COLUMN_PLATE + "," +
                ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + WhitelistEntry.TABLE_NAME + "," + ConciergeContract.ApartmentEntry.TABLE_NAME +
                " WHERE " + ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry.COLUMN_APARTMENT_ID +
                " AND " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry.COLUMN_PLATE + " <> ''" +
                " AND " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry.COLUMN_PLATE + " IS NOT NULL";


        Cursor c;
        try {
            c = db.rawQuery(query,null);

        }catch (Exception e){
            c = null;
        }


        //SE GENERA EL EXCEL PARA REALIZAR LA SUBIDA MEDIANTE ISAPI
        HSSFWorkbook XlsBookPlates = new HSSFWorkbook();
        HSSFSheet XlsSheetPlates = XlsBookPlates.createSheet();


        int i = 0;
        if (c != null) {
            HSSFRow firtsRow = XlsSheetPlates.createRow(0);


            HSSFCell firstRowCell0 = firtsRow.createCell(0);
            HSSFRichTextString title0 = new HSSFRichTextString("No.");
            firstRowCell0.setCellValue(title0);

            HSSFCell firstRowCell1 = firtsRow.createCell(1);
            HSSFRichTextString title1 = new HSSFRichTextString("Plate No.");
            firstRowCell1.setCellValue(title1);

            HSSFCell firstRowCell2 = firtsRow.createCell(2);
            HSSFRichTextString title2 = new HSSFRichTextString("Group(0 block list, 1 allow list)");
            firstRowCell2.setCellValue(title2);

            HSSFCell firstRowCell3 = firtsRow.createCell(3);
            HSSFRichTextString title3 = new HSSFRichTextString("Effective Start Date (Format: YYYY-MM-DD, e.g., 2017-12-07)");
            firstRowCell3.setCellValue(title3);


            HSSFCell firstRowCell4 = firtsRow.createCell(4);
            HSSFRichTextString title4 = new HSSFRichTextString("Effective End Date (Format: YYYY-MM-DD, e.g., 2017-12-07)");
            firstRowCell4.setCellValue(title4);




            while (c.moveToNext()){
                //FOR EACH PLATE WE NEED TO GENERATE ALTERNATIVES BY LETER THAT CAN BE READER
                //LIKE NUMBER, FOR EXAMPLE: CBGB45 THE B CHARACTER CAN BE READER LIKE 8, WE
                //NEED TO GENERATE ALL ALTERNATIVES: C8GB45 C8G845 CBG845
                plate = c.getString(1);
                plates.clear();

                //PATENTE NORMAL SE AGREGAN ALTERNATIVAS DE LETRAS
                plates.add(plate);
                generates_plates_alternatives(plate,0,plate.length());

                //SE AGREGA ALTERNATIVA CON '-' DEL MEDIO
                String plate_aux1 = ((plate.toString().substring(0,2)).concat("-")).concat(plate.toString().substring(2,plate.toString().length()));
                plates.add(plate_aux1);
                generates_plates_alternatives(plate_aux1,0,plate_aux1.length());

                //SE ELIMINA EL PRIMER CARACTER SI CONTIENE UNA L AL PRINCIPIO
                if(plate.substring(0,1).contentEquals("L")){
                    String plate_aux2 = plate.toString().substring(1,plate.toString().length());
                    plates.add(plate_aux2);
                    generates_plates_alternatives(plate_aux2,0,plate_aux2.length());

                    String plate_aux3 = ((plate_aux2.toString().substring(0,1)).concat("-")).concat(plate_aux2.toString().substring(1,plate_aux2.toString().length()));
                    plates.add(plate_aux3);
                    generates_plates_alternatives(plate_aux3,0,plate_aux3.length());
                }


                for (String plt:plates) {

                    /*
                    No.
                    Plate No.
                    Group(0 block list, 1 allow list)
                    Start Date (Format: YYYY-MM-DD, e.g., 2017-12-07)
                    Expiry Date (Format: YYYY-MM-DD, e.g., 2017-12-07)
                    Card No.*/

                    i = i + 1;
                    HSSFRow row = XlsSheetPlates.createRow(i);

                    //NRO
                    HSSFCell cellNro = row.createCell(0);
                    HSSFRichTextString Snro = new HSSFRichTextString(Integer.toString(i));
                    cellNro.setCellValue(Snro);

                    //PLATE
                    HSSFCell cellPlate = row.createCell(1);
                    HSSFRichTextString plate = new HSSFRichTextString(plt);
                    cellPlate.setCellValue(plate);

                    //ALLOW OR BLACK
                    HSSFCell cellBlackAllow = row.createCell(2);
                    HSSFRichTextString SblackAllow = new HSSFRichTextString(Integer.toString(1));
                    cellBlackAllow.setCellValue(SblackAllow);

                    //START DATE
                    HSSFCell cellStartDate = row.createCell(3);
                    HSSFRichTextString startDate = new HSSFRichTextString("2020-01-01");
                    cellStartDate.setCellValue(startDate);

                    //END DATE
                    HSSFCell cellEndDate = row.createCell(4);
                    HSSFRichTextString endDate = new HSSFRichTextString("2030-12-31");
                    cellEndDate.setCellValue(endDate);




                }
            }
            c.close();

            //SE GENERA EL ARCHIVO XLS CON PATENTES
            try {

                //File sdCard = Environment.getExternalStorageDirectory();
                //File dir = new File (sdCard.getAbsolutePath());
                File dir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
                File file = new File(dir, "plates_v2.xls");
                FileOutputStream filePlates = new FileOutputStream(file);
                XlsBookPlates.write(filePlates);
                filePlates.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void generate_file_v1(){
        dbHelper = new ConciergeDbHelper(mContext);
        db = DatabaseManager.getInstance().openDatabase();

        Log.d(LOG_TAG, "Starting sync ISAPI alpr");

        final String query = "SELECT " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry._ID + "," +
                ResidentVehicleEntry.TABLE_NAME + "." +ResidentVehicleEntry.COLUMN_PLATE + "," +
                ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + ResidentVehicleEntry.TABLE_NAME + "," + ConciergeContract.ApartmentEntry.TABLE_NAME +
                " WHERE " + ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry.COLUMN_APARTMENT_ID +
                " AND " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry.COLUMN_ACTIVE + " = " + IS_ACTIVE +

                " UNION SELECT " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry._ID + "," +
                WhitelistEntry.TABLE_NAME + "." +WhitelistEntry.COLUMN_PLATE + "," +
                ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + WhitelistEntry.TABLE_NAME + "," + ConciergeContract.ApartmentEntry.TABLE_NAME +
                " WHERE " + ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry.COLUMN_APARTMENT_ID +
                " AND " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry.COLUMN_PLATE + " <> ''" +
                " AND " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry.COLUMN_PLATE + " IS NOT NULL";

        Cursor c;
        try {
            c = db.rawQuery(query,null);

        }catch (Exception e){
            c = null;
        }


        //SE GENERA EL EXCEL PARA REALIZAR LA SUBIDA MEDIANTE ISAPI
        HSSFWorkbook XlsBookPlates = new HSSFWorkbook();
        HSSFSheet XlsSheetPlates = XlsBookPlates.createSheet();


        int i = 0;
        if (c != null) {
            HSSFRow firtsRow = XlsSheetPlates.createRow(0);

            HSSFCell firstRowCell0 = firtsRow.createCell(0);
            HSSFRichTextString title0 = new HSSFRichTextString("No.");
            firstRowCell0.setCellValue(title0);

            HSSFCell firstRowCell1 = firtsRow.createCell(1);
            HSSFRichTextString title1 = new HSSFRichTextString("Plate No.");
            firstRowCell1.setCellValue(title1);

            HSSFCell firstRowCell2 = firtsRow.createCell(2);
            HSSFRichTextString title2 = new HSSFRichTextString("Group(0 block list, 1 allow list)");
            firstRowCell2.setCellValue(title2);

            HSSFCell firstRowCell3 = firtsRow.createCell(3);
            HSSFRichTextString title3 = new HSSFRichTextString("Expiry Date (Format: YYYY-MM-DD, e.g., 2017-12-07)");
            firstRowCell3.setCellValue(title3);

            HSSFCell firstRowCell4 = firtsRow.createCell(4);
            HSSFRichTextString title4 = new HSSFRichTextString("Card No.");
            firstRowCell4.setCellValue(title4);

            while (c.moveToNext()){
                //FOR EACH PLATE WE NEED TO GENERATE ALTERNATIVES BY LETER THAT CAN BE READER
                //LIKE NUMBER, FOR EXAMPLE: CBGB45 THE B CHARACTER CAN BE READER LIKE 8, WE
                //NEED TO GENERATE ALL ALTERNATIVES: C8GB45 C8G845 CBG845
                plate = c.getString(1);
                plates.clear();

                //PATENTE NORMAL SE AGREGAN ALTERNATIVAS DE LETRAS
                plates.add(plate);
                generates_plates_alternatives(plate,0,plate.length());

                //SE AGREGA ALTERNATIVA CON '-' DEL MEDIO
                String plate_aux1 = ((plate.toString().substring(0,2)).concat("-")).concat(plate.toString().substring(2,plate.toString().length()));
                plates.add(plate_aux1);
                generates_plates_alternatives(plate_aux1,0,plate_aux1.length());

                //SE ELIMINA EL PRIMER CARACTER SI CONTIENE UNA L AL PRINCIPIO
                if(plate.substring(0,1).contentEquals("L")){
                    String plate_aux2 = plate.toString().substring(1,plate.toString().length());
                    plates.add(plate_aux2);
                    generates_plates_alternatives(plate_aux2,0,plate_aux2.length());

                    String plate_aux3 = ((plate_aux2.toString().substring(0,1)).concat("-")).concat(plate_aux2.toString().substring(1,plate_aux2.toString().length()));
                    plates.add(plate_aux3);
                    generates_plates_alternatives(plate_aux3,0,plate_aux3.length());
                }


                for (String plt:plates) {

                    /*
                    No.
                    Plate No.
                    Group(0 block list, 1 allow list)
                    Expiry Date (Format: YYYY-MM-DD, e.g., 2017-12-07)
                    Card No.*/

                    i = i + 1;
                    HSSFRow row = XlsSheetPlates.createRow(i);

                    //NRO
                    HSSFCell cellNro = row.createCell(0);
                    HSSFRichTextString Snro = new HSSFRichTextString(Integer.toString(i));
                    cellNro.setCellValue(Snro);

                    //PLATE
                    HSSFCell cellPlate = row.createCell(1);
                    HSSFRichTextString plate = new HSSFRichTextString(plt);
                    cellPlate.setCellValue(plate);

                    //ALLOW OR BLACK
                    HSSFCell cellBlackAllow = row.createCell(2);
                    HSSFRichTextString SblackAllow = new HSSFRichTextString(Integer.toString(1));
                    cellBlackAllow.setCellValue(SblackAllow);

                    //END DATE
                    HSSFCell cellEndDate = row.createCell(3);
                    HSSFRichTextString endDate = new HSSFRichTextString("2030-12-31");
                    cellEndDate.setCellValue(endDate);

                }
            }
            c.close();

            //SE GENERA EL ARCHIVO XLS CON PATENTES
            try {

                //File sdCard = Environment.getExternalStorageDirectory();
                //File dir = new File (sdCard.getAbsolutePath());
                File dir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
                File file = new File(dir, "plates_v1.xls");
                FileOutputStream filePlates = new FileOutputStream(file);
                XlsBookPlates.write(filePlates);
                filePlates.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void generate_file_v3(){
        dbHelper = new ConciergeDbHelper(mContext);
        db = DatabaseManager.getInstance().openDatabase();

        Log.d(LOG_TAG, "Starting sync ISAPI alpr");

        final String query = "SELECT " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry._ID + "," +
                ResidentVehicleEntry.TABLE_NAME + "." +ResidentVehicleEntry.COLUMN_PLATE + "," +
                ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + ResidentVehicleEntry.TABLE_NAME + "," + ConciergeContract.ApartmentEntry.TABLE_NAME +
                " WHERE " + ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry.COLUMN_APARTMENT_ID +
                " AND " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry.COLUMN_ACTIVE + " = " + IS_ACTIVE +

                " UNION SELECT " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry._ID + "," +
                WhitelistEntry.TABLE_NAME + "." +WhitelistEntry.COLUMN_PLATE + "," +
                ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + WhitelistEntry.TABLE_NAME + "," + ConciergeContract.ApartmentEntry.TABLE_NAME +
                " WHERE " + ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry.COLUMN_APARTMENT_ID +
                " AND " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry.COLUMN_PLATE + " <> ''" +
                " AND " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry.COLUMN_PLATE + " IS NOT NULL";


        Cursor c;
        try {
            c = db.rawQuery(query,null);

        }catch (Exception e){
            c = null;
        }


        //SE GENERA EL EXCEL PARA REALIZAR LA SUBIDA MEDIANTE ISAPI
        HSSFWorkbook XlsBookPlates = new HSSFWorkbook();
        HSSFSheet XlsSheetPlates = XlsBookPlates.createSheet();


        int i = 0;
        if (c != null) {
            HSSFRow firtsRow = XlsSheetPlates.createRow(0);


            HSSFCell firstRowCell0 = firtsRow.createCell(0);
            HSSFRichTextString title0 = new HSSFRichTextString("No.");
            firstRowCell0.setCellValue(title0);

            HSSFCell firstRowCell1 = firtsRow.createCell(1);
            HSSFRichTextString title1 = new HSSFRichTextString("Plate No.");
            firstRowCell1.setCellValue(title1);

            HSSFCell firstRowCell2 = firtsRow.createCell(2);
            HSSFRichTextString title2 = new HSSFRichTextString("Group(0 block list, 1 allow list)");
            firstRowCell2.setCellValue(title2);

            HSSFCell firstRowCell3 = firtsRow.createCell(3);
            HSSFRichTextString title3 = new HSSFRichTextString("Effective Start Date (Format: YYYY-MM-DD, e.g., 2017-12-07)");
            firstRowCell3.setCellValue(title3);


            HSSFCell firstRowCell4 = firtsRow.createCell(4);
            HSSFRichTextString title4 = new HSSFRichTextString("Effective End Date (Format: YYYY-MM-DD, e.g., 2017-12-07)");
            firstRowCell4.setCellValue(title4);


            //CARD
            HSSFCell firstRowCell5 = firtsRow.createCell(5);
            HSSFRichTextString title5 = new HSSFRichTextString("Card");
            firstRowCell5.setCellValue(title5);


            while (c.moveToNext()){
                //FOR EACH PLATE WE NEED TO GENERATE ALTERNATIVES BY LETER THAT CAN BE READER
                //LIKE NUMBER, FOR EXAMPLE: CBGB45 THE B CHARACTER CAN BE READER LIKE 8, WE
                //NEED TO GENERATE ALL ALTERNATIVES: C8GB45 C8G845 CBG845
                plate = c.getString(1);
                plates.clear();

                //PATENTE NORMAL SE AGREGAN ALTERNATIVAS DE LETRAS
                plates.add(plate);
                generates_plates_alternatives(plate,0,plate.length());

                //SE AGREGA ALTERNATIVA CON '-' DEL MEDIO
                String plate_aux1 = ((plate.toString().substring(0,2)).concat("-")).concat(plate.toString().substring(2,plate.toString().length()));
                plates.add(plate_aux1);
                generates_plates_alternatives(plate_aux1,0,plate_aux1.length());

                //SE ELIMINA EL PRIMER CARACTER SI CONTIENE UNA L AL PRINCIPIO
                if(plate.substring(0,1).contentEquals("L")){
                    String plate_aux2 = plate.toString().substring(1,plate.toString().length());
                    plates.add(plate_aux2);
                    generates_plates_alternatives(plate_aux2,0,plate_aux2.length());

                    String plate_aux3 = ((plate_aux2.toString().substring(0,1)).concat("-")).concat(plate_aux2.toString().substring(1,plate_aux2.toString().length()));
                    plates.add(plate_aux3);
                    generates_plates_alternatives(plate_aux3,0,plate_aux3.length());
                }


                for (String plt:plates) {

                    /*
                    No.
                    Plate No.
                    Group(0 block list, 1 allow list)
                    Start Date (Format: YYYY-MM-DD, e.g., 2017-12-07)
                    Expiry Date (Format: YYYY-MM-DD, e.g., 2017-12-07)
                    Card No.*/

                    i = i + 1;
                    HSSFRow row = XlsSheetPlates.createRow(i);

                    //NRO
                    HSSFCell cellNro = row.createCell(0);
                    HSSFRichTextString Snro = new HSSFRichTextString(Integer.toString(i));
                    cellNro.setCellValue(Snro);

                    //PLATE
                    HSSFCell cellPlate = row.createCell(1);
                    HSSFRichTextString plate = new HSSFRichTextString(plt);
                    cellPlate.setCellValue(plate);

                    //ALLOW OR BLACK
                    HSSFCell cellBlackAllow = row.createCell(2);
                    HSSFRichTextString SblackAllow = new HSSFRichTextString(Integer.toString(1));
                    cellBlackAllow.setCellValue(SblackAllow);

                    //START DATE
                    HSSFCell cellStartDate = row.createCell(3);
                    HSSFRichTextString startDate = new HSSFRichTextString("2020-01-01");
                    cellStartDate.setCellValue(startDate);

                    //END DATE
                    HSSFCell cellEndDate = row.createCell(4);
                    HSSFRichTextString endDate = new HSSFRichTextString("2030-12-31");
                    cellEndDate.setCellValue(endDate);

                    //CARD
                    HSSFCell cellCards = row.createCell(5);
                    HSSFRichTextString Card = new HSSFRichTextString("0000");
                    cellCards.setCellValue(Card);



                }
            }
            c.close();

            //SE GENERA EL ARCHIVO XLS CON PATENTES
            try {

                //File sdCard = Environment.getExternalStorageDirectory();
                //File dir = new File (sdCard.getAbsolutePath());
                File dir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
                File file = new File(dir, "plates_v3.xls");
                FileOutputStream filePlates = new FileOutputStream(file);
                XlsBookPlates.write(filePlates);
                filePlates.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    public void sync() {

        generate_file_v1();
        generate_file_v2();
        generate_file_v3();
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String IP_CAMERAS = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_ips_cameras_key),"");

            String[] arrIpCameras = IP_CAMERAS.split(";", -1);

            for (String IP_CAMERA_AND_VERSION : arrIpCameras){
                String IP_CAMERA = IP_CAMERA_AND_VERSION.split("_", -1)[0];
                String VERSION_FILE = IP_CAMERA_AND_VERSION.split("_", -1)[1];

                if(!IP_CAMERA.equals("")){

                    String auth = USER_CAMERA+":"+PASS_CAMERA;
                    byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.UTF_8),0);
                    String authHeaderValue = "Basic " + new String(encodedAuth);

                    URL url = new URL("http://"+IP_CAMERA+":80/ISAPI/Traffic/channels/1/licensePlateAuditData");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("PUT");
                    urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko");
                    urlConnection.setRequestProperty("Content-Type","text/plain");
                    urlConnection.setRequestProperty("Authorization", authHeaderValue);
                    urlConnection.setConnectTimeout(20000);
                    urlConnection.setReadTimeout(20000);
                    urlConnection.setDoInput(true);
                    urlConnection.connect();

                    String file_name = "";
                    if(VERSION_FILE.equals("V1")){
                        file_name = "plates_v1.xls";
                    }
                    if(VERSION_FILE.equals("V2")){
                        file_name = "plates_v2.xls";
                    }
                    if(VERSION_FILE.equals("V3")){
                        file_name = "plates_v3.xls";
                    }


                    File dir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
                    File file = new File(dir, file_name);

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

            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }
}
