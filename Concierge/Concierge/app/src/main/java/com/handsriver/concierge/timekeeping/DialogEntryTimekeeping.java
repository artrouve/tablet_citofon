package com.handsriver.concierge.timekeeping;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.insertsTables.InsertEntryTimekeeping;
import com.handsriver.concierge.home.HomeFragment;
import com.handsriver.concierge.utilities.FormatICAO9303;
import com.handsriver.concierge.utilities.Utility;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 22-03-17.
 */

public class DialogEntryTimekeeping extends DialogFragment {
    TextView textViewRut;
    TextView textViewFullName;
    TextView textViewEntry;
    String rut;
    String fullName;
    String entry;
    int porterId;

    public static final String PREFS_NAME = "PorterPrefs";
    public static final int DEF_VALUE = 0;

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

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_timekeeping,null);

        builder.setTitle(R.string.entryTitle);

        builder.setMessage(R.string.messageEntryPorter);

        builder.setView(content);

        textViewRut = (TextView) content.findViewById(R.id.textViewRutDialog);
        textViewFullName = (TextView) content.findViewById(R.id.textViewFullNameDialog);
        textViewEntry = (TextView) content.findViewById(R.id.textViewEntryDialog);

        rut = getArguments().getString("rut");

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
        entry = df.format(calendar.getTime());

        String[] projection = {
                PorterEntry.COLUMN_FIRST_NAME,
                PorterEntry.COLUMN_LAST_NAME,
                PorterEntry.COLUMN_PORTER_ID_SERVER,
                PorterEntry.COLUMN_RUT
        };

        Cursor c;
        try {
            SelectToDB selectPorters = new SelectToDB(PorterEntry.TABLE_NAME,projection,null,null,null,null,null,null);
            c = selectPorters.execute().get();
        }catch (Exception e){
            c = null;
        }

        textViewRut.setText(FormatICAO9303.rutFormat(rut));
        textViewEntry.setText(entry);

        if (c != null){
            while (c.moveToNext()){
                String rutFormat = c.getString(c.getColumnIndex(PorterEntry.COLUMN_RUT)).replace(".","").replace("-","");
                if (rut.equals(rutFormat)){
                    fullName = c.getString(c.getColumnIndex(PorterEntry.COLUMN_FIRST_NAME))+" "+c.getString(c.getColumnIndex(PorterEntry.COLUMN_LAST_NAME));
                    textViewFullName.setText(fullName);
                    porterId = c.getInt(c.getColumnIndex(PorterEntry.COLUMN_PORTER_ID_SERVER));
                }
            }
            c.close();
        }


        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int porterIdServer;
                int gatewayId;

                SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar),DEF_VALUE);

                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                gatewayId = Integer.parseInt(settingsPrefs.getString(getString(R.string.pref_id_gateway_key),"0"));

                InsertEntryTimekeeping entryTimekeeping = new InsertEntryTimekeeping(null, Utility.changeDateFormatDatabase(entry),gatewayId,porterId,porterIdServer,getContext());
                entryTimekeeping.execute();

                HomeFragment fragmentHome = new HomeFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentHome).commit();
                NavigationView navigationView = (NavigationView)getActivity().findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.home);
                Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                toolbar.setTitle(getString(R.string.app_name));

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }
}
