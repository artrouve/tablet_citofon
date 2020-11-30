package com.handsriver.concierge.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.vehicles.VehiclePlateAdapter;
import com.handsriver.concierge.vehicles.VehiclePlateDetectedOper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by Created by alain_r._trouve_silva after 26-07-17.
 */

public class GetPlateDetection extends AsyncTask<String,Void,Boolean>
{
    ProgressBar indeterminateBar;
    VehiclePlateDetectedOper DetectedPlate;
    VehiclePlateAdapter vehiclePlateAdapter;
    ListView platesLists;

    Context context;

    public GetPlateDetection(ProgressBar indeterminateBar, VehiclePlateDetectedOper DetectedPlate, VehiclePlateAdapter vehiclePlateAdapter, Context context, ListView platesLists) {
        this.indeterminateBar = indeterminateBar;
        this.DetectedPlate = DetectedPlate;
        this.vehiclePlateAdapter = vehiclePlateAdapter;
        this.platesLists = platesLists;
        this.context = context;



    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        indeterminateBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        //DETECCION DE PATENTES:
        if(true){

            try{
                String idGateway = params[0];
                String urlParameters = "idGateway="+idGateway;
                byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
                int postDataLength = postData.length;
                //String BASE_URL = "https://salpr.citofon.cl/ftp/getPlates.php";
                String BASE_URL = "https://salpr.citofon.cl/ftp/getPlate.php";



                Uri buildUri = Uri.parse(BASE_URL).buildUpon().build();
                URL url = new URL(buildUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                urlConnection.setConnectTimeout(4000);
                urlConnection.setReadTimeout(4000);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.write(postData);
                wr.flush();
                wr.close();

                InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream(),"UTF-8");
                if (inputStream == null) {
                    // Nothing to do.
                    return false;
                }
                reader = new BufferedReader(inputStream);

                String line;
                String json = "";


                while ((line = reader.readLine()) != null) {

                    json = json + line;


                }

                JSONObject Json = new JSONObject(json);

                int i =  0;
                for(i=0;i<Json.getJSONArray("patentes").length();i++){


                    String url_ = Json.getJSONArray("patentes").get(i).toString().split(";")[2];
                    String plate = Json.getJSONArray("patentes").get(i).toString().split(";")[0];
                    String datetime = Json.getJSONArray("patentes").get(i).toString().split(";")[1];

                    SharedPreferences plateDetectionPref = this.context.getSharedPreferences(this.DetectedPlate.PREFS_PLATE_DETECTION_NAME, Context.MODE_PRIVATE);

                    String platesDetection = plateDetectionPref.getString(this.context.getString(R.string.platesDetectionReceived), "");
                    platesDetection = plate + "@" + datetime + "@" + url_ + ";" + platesDetection ;

                    SharedPreferences.Editor editor = plateDetectionPref.edit();
                    editor.putString(this.context.getString(R.string.platesDetectionReceived),platesDetection);
                    editor.apply();

                }
                return true;

            }catch (Exception e) {
                Log.e("LicensePlate", "Error ", e);
                return false;
            }

        }
        return false;



    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        indeterminateBar.setVisibility(ProgressBar.GONE);

        this.DetectedPlate.ExitVehicles();
        this.vehiclePlateAdapter.refreshEvents(this.DetectedPlate.getItemsPlateDetected());
        this.vehiclePlateAdapter.notifyDataSetChanged();


        /*
        this.vehiclePlateAdapter = new VehiclePlateAdapter(this.context,DetectedPlate.getItemsPlateDetected());
        this.platesLists.setAdapter(vehiclePlateAdapter);
        this.vehiclePlateAdapter.notifyDataSetChanged();

         */

        /*
        expandableAdapter = baseFragmentParent.setupEXLVAdapter();
        baseFragmentParent.setAdapter(expandableAdapter);
        expandableAdapter.notifyDataSetChanged();

        this.vehiclePlateAdapter = new
        this.vehiclePlateAdapter.refreshEvents(this.DetectedPlate.getItemsPlateDetected());
        this.vehiclePlateAdapter.notifyDataSetChanged();
        */
        /*
        vehiclePlateAdapter = new VehiclePlateAdapter(this.context,DetectedPlate.getItemsPlateDetected());
        platesLists.setAdapter(vehiclePlateAdapter);


        */
    }
}
