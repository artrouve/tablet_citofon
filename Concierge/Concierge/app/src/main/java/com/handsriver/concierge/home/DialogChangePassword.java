package com.handsriver.concierge.home;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdatePorters;
import com.handsriver.concierge.utilities.BCrypt;
import com.handsriver.concierge.utilities.Utility;

/**
 * Created by Created by alain_r._trouve_silva after 19-03-18.
 */

public class DialogChangePassword extends DialogFragment {

    LinearLayout linearChangePassword;
    TextInputEditText editTextPassword;
    TextInputEditText editTextRepPassword;
    Button buttonPositive;
    TextView textViewAlert;
    public static final int DEF_VALUE = 0;
    public static final String PREFS_NAME = "PorterPrefs";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.password);
        builder.setMessage(R.string.passwordText);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_change_password,null);

        linearChangePassword = (LinearLayout) content.findViewById(R.id.linearChangePassword);
        editTextPassword = (TextInputEditText) content.findViewById(R.id.editTextPassword);
        editTextRepPassword = (TextInputEditText) content.findViewById(R.id.editTextRepPassword);
        textViewAlert = (TextView) content.findViewById(R.id.textViewAlertDialog);

        linearChangePassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        builder.setView(content);

        builder.setPositiveButton(R.string.password, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(editTextPassword.getText().toString().equals(editTextRepPassword.getText().toString())){

                    int porterIdServer;

                    SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar), DEF_VALUE);

                    UpdatePorters updatePass = new UpdatePorters(porterIdServer,getContext(), editTextRepPassword.getText().toString());
                    updatePass.execute();

                }
            }
        });

        builder.setNegativeButton(R.string.cancel,null);

        editTextRepPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editTextPassword.getText().toString().equals(editTextRepPassword.getText().toString())){
                    if(editTextPassword.getText().toString().length() >= 6 && editTextRepPassword.getText().toString().length() >= 6){
                        buttonPositive.setEnabled(true);
                        textViewAlert.setVisibility(View.GONE);
                    }
                    else{
                        textViewAlert.setVisibility(View.VISIBLE);
                        textViewAlert.setText(R.string.passwordAlert2);
                    }


                }
                else{
                    if(editTextRepPassword.getText().toString().length() > 0){
                        textViewAlert.setVisibility(View.VISIBLE);
                        textViewAlert.setText(R.string.passwordAlert);
                    }
                    else{
                        textViewAlert.setVisibility(View.GONE);
                    }
                    if(buttonPositive.isEnabled()){
                        buttonPositive.setEnabled(false);
                    }
                }
            }
        });

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editTextPassword.getText().toString().equals(editTextRepPassword.getText().toString())){
                    if(editTextPassword.getText().toString().length() >= 6 && editTextRepPassword.getText().toString().length() >= 6){
                        buttonPositive.setEnabled(true);
                        textViewAlert.setVisibility(View.GONE);
                    }
                    else{
                        textViewAlert.setVisibility(View.VISIBLE);
                        textViewAlert.setText(R.string.passwordAlert2);
                    }


                }
                else{
                    if(editTextPassword.getText().toString().length() > 0){
                        textViewAlert.setVisibility(View.VISIBLE);
                        textViewAlert.setText(R.string.passwordAlert);
                    }
                    else{
                        textViewAlert.setVisibility(View.GONE);
                    }
                    if(buttonPositive.isEnabled()){
                        buttonPositive.setEnabled(false);
                    }
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        buttonPositive= alert.getButton(AlertDialog.BUTTON_POSITIVE);
        buttonPositive.setEnabled(false);


        return alert;
    }
}
