package com.handsriver.concierge.utilities;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handsriver.concierge.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by Created by alain_r._trouve_silva after 26-07-17.
 */

public class LicensePlateCheck extends AsyncTask<String,Void,Boolean>
{
    TextView textViewAlertDialog;
    TextView textViewLicensePlate;
    ProgressBar indeterminateBar;

    public LicensePlateCheck(TextView textViewAlertDialog, TextView textViewLicensePlate, ProgressBar indeterminateBar) {
        this.textViewAlertDialog = textViewAlertDialog;
        this.textViewLicensePlate = textViewLicensePlate;
        this.indeterminateBar = indeterminateBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        indeterminateBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if(params[0].length() == 6){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try{
                String txtLetras = params[0].substring(0,2);
                String txtNumeros1 = params[0].substring(2,4);
                String txtNumeros2 = params[0].substring(4,6);

                //String urlParameters = "accion=buscar"+"&txtLetras="+txtLetras+"&txtNumeros1="+txtNumeros1+"&txtNumeros2="+txtNumeros2;
                String urlParameters = "accion=buscar"+"&patente="+txtLetras+txtNumeros1+txtNumeros2;
                byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
                int postDataLength = postData.length;
                String BASE_URL = "http://consultawebvehiculos.carabineros.cl";
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().build();
                URL url = new URL(buildUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
                urlConnection.setConnectTimeout(3000);
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

                while ((line = reader.readLine()) != null) {
                    Log.e("LogsAndroid",line);
                    if (line.contains("LA PATENTE PRESENTA ENCARGO POLICIAL")){
                        return true;
                    }
                }

                return false;

            }catch (IOException e) {
                Log.e("LicensePlate", "Error ", e);
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        indeterminateBar.setVisibility(ProgressBar.GONE);
        if (aBoolean){
            textViewAlertDialog.setVisibility(View.VISIBLE);
            textViewAlertDialog.setText("!!!!ALERTA!!!!, Se esta registrando un VEHÍCULO CON ENCARGO POR ROBO. Llame al SEBV de Carabineros al +56229221021 o al +56229221019, o al correo departamento.encargos@carabineros.cl");
            textViewLicensePlate.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_error,0);
        }
    }
}
