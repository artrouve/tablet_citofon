package com.handsriver.concierge.login;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.PorterEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.home.MainActivity;
import com.handsriver.concierge.utilities.BCrypt;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 10-03-18.
 */

public class DialogLogIn extends DialogFragment {

    ArrayList<Porter> mPorter;
    PortersAdapter portersAdapter;
    ListView listPorters;
    EditText editTextPassword;
    Porter porterSelected;
    public static final String PREFS_NAME = "PorterPrefs";
    public static final String IS_ACTIVE = "1";
    Button buttonPositive;
    boolean selectItemListView;
    LinearLayout linearLogin;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mPorter = new ArrayList<Porter>();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.textLogin);
        selectItemListView = false;

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_login,null);

        builder.setView(content);

        builder.setTitle(R.string.login);

        final String query = "SELECT " + PorterEntry._ID + "," +
                PorterEntry.COLUMN_PORTER_ID_SERVER + "," +
                PorterEntry.COLUMN_FIRST_NAME + "," +
                PorterEntry.COLUMN_LAST_NAME + "," +
                PorterEntry.COLUMN_RUT + "," +
                PorterEntry.COLUMN_PASSWORD +
                " FROM " + PorterEntry.TABLE_NAME +
                " WHERE " + PorterEntry.COLUMN_ACTIVE + " = " + IS_ACTIVE +
                " ORDER BY " + PorterEntry.COLUMN_FIRST_NAME + " ASC";

        Cursor c;

        try {
            SelectToDBRaw selectwhitelist = new SelectToDBRaw(query,null);
            c = selectwhitelist.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null) {
            while (c.moveToNext()){
                Porter porter = new Porter();
                porter.setFirstName(c.getString(c.getColumnIndex(PorterEntry.COLUMN_FIRST_NAME)));
                porter.setLastName(c.getString(c.getColumnIndex(PorterEntry.COLUMN_LAST_NAME)));
                porter.setRut(c.getString(c.getColumnIndex(PorterEntry.COLUMN_RUT)));
                porter.setPassword(c.getString(c.getColumnIndex(PorterEntry.COLUMN_PASSWORD)));
                porter.setPorterIdServer(c.getInt(c.getColumnIndex(PorterEntry.COLUMN_PORTER_ID_SERVER)));
                mPorter.add(porter);
            }
            c.close();
            portersAdapter = new PortersAdapter(getActivity(),mPorter);
        }
        linearLogin = (LinearLayout) content.findViewById(R.id.linearLogin);
        editTextPassword = (EditText) content.findViewById(R.id.editTextPassword);
        listPorters = (ListView) content.findViewById(R.id.porterListDialog);
        listPorters.setAdapter(portersAdapter);

        builder.setPositiveButton(R.string.text_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setNegativeButton(R.string.cancel,null);

        listPorters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                porterSelected = (Porter) parent.getItemAtPosition(position);
                portersAdapter.setSelectedIndex(position);
                buttonPositive.setEnabled(true);
                selectItemListView = true;

            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        buttonPositive= alert.getButton(AlertDialog.BUTTON_POSITIVE);
        buttonPositive.setEnabled(false);
        buttonPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               checkUser();
            }
        });

        linearLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        editTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    checkUser();
                    return true;
                }
                return false;            }
        });


        return alert;
    }

    private void setPorterSharedPrefs(String firstName, String lastName, String rut, int porterIdServer){
        SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = porterPrefs.edit();
        editor.putString(getString(R.string.fullNameVar),firstName+" "+lastName);
        editor.putString(getString(R.string.rutVar),rut);
        editor.putInt(getString(R.string.porterIdServerVar),porterIdServer);
        editor.apply();
    }

    private void checkUser(){
        if(selectItemListView){
            if(BCrypt.checkpw(editTextPassword.getText().toString(),porterSelected.getPassword())){

                Intent intent = new Intent(getActivity().getApplicationContext(),MainActivity.class);
                startActivity(intent);
                editTextPassword.setText(null);

                setPorterSharedPrefs(porterSelected.getFirstName(),porterSelected.getLastName(),porterSelected.getRut(),porterSelected.getPorterIdServer());
            }
            else{
                Toast.makeText(getActivity().getApplicationContext(), "Contraseña incorrecta, inténtelo nuevamente", Toast.LENGTH_LONG).show();
                editTextPassword.setText(null);
            }
        }
        else{
            Toast.makeText(getActivity().getApplicationContext(), "Seleccione su usuario y ingrese su contraseña", Toast.LENGTH_LONG).show();

        }
    }

}
