package com.handsriver.concierge.login;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.settings.SettingsActivity;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ConfigureSyncAccount.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            showDialog(this);

            return true;
        }
        if (id == R.id.action_sync){
            item.setEnabled(false);
            item.getIcon().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            ConfigureSyncAccount.syncImmediately(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showDialog(final Context context)
    {

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_password_settings, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.passwordInput);

        alertDialogBuilder.setTitle(R.string.titlePasswordSetting);
        alertDialogBuilder.setMessage(R.string.messagePasswordSetting);
        alertDialogBuilder.setIcon(R.drawable.ic_warning);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.accept,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String user_text = (userInput.getText()).toString();

                                if (user_text.equals("Concierge#Tab$"))
                                {
                                    startActivity(new Intent(context, SettingsActivity.class));

                                }
                                else{
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setIcon(R.drawable.ic_error);
                                    builder.setTitle(R.string.errorTitlePasswordSetting);
                                    builder.setMessage(R.string.errorPasswordSetting);
                                    builder.setNegativeButton(R.string.cancel, null);
                                    builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            showDialog(context);
                                        }
                                    });
                                    builder.create().show();
                                }
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.dismiss();
                            }

                        }

                );

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }
}
