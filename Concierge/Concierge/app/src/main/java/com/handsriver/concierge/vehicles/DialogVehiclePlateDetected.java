package com.handsriver.concierge.vehicles;


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
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.insertsTables.InsertVehicle;
import com.handsriver.concierge.database.insertsTables.InsertVisits;
import com.handsriver.concierge.email_notifications.SendEmailNotifications;
import com.handsriver.concierge.home.HomeFragment;
import com.handsriver.concierge.sync.ConfigureSyncAccount;
import com.handsriver.concierge.utilities.LicensePlateCheck;
import com.handsriver.concierge.visits.Visit;
import com.handsriver.concierge.visits.VisitsAdapterDialog;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Created by alain_r._trouve_silva after 27-02-17.
 */

public class DialogVehiclePlateDetected extends DialogFragment {



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_select_plate_detected,null);

        //getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        /*
        builder.setTitle(R.string.plates_detected_selected_show);
        builder.setMessage("");
        */

        builder.setView(content);

        ImageView imagePlate = (ImageView) content.findViewById(R.id.plateImageZoomView);

        String url = getArguments().getString("urlBase");

        Picasso.get().load(url).into(imagePlate);

        builder.setPositiveButton(R.string.plates_detected_selected_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //SE ESTABLECE LA CORRECTA SELECCION DE LA PATENTE, SE DEBE SETEAR LOS
                //VALORES DONDE CORRESPONDE

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = (int)(getContext().getResources().getDisplayMetrics().widthPixels);
        int height = (int)(getContext().getResources().getDisplayMetrics().heightPixels);
        getDialog().getWindow().setLayout(width, height);
    }

}
