package com.handsriver.concierge.residents;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.insertsTables.InsertResidents;
import com.handsriver.concierge.database.updatesTables.UpdateCodeAuth;
import com.handsriver.concierge.database.updatesTables.UpdateResidents;
import com.handsriver.concierge.home.HomeFragment;
import com.handsriver.concierge.utilities.Utility;

import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 14-03-17.
 */

public class DialogCodeAuthResident extends DialogFragment{

    TextView textViewApartment;
    TextView textViewFullName;
    TextView textViewEmail;
    String apartment;
    String fullName;
    String email;
    long id;

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
        View content = inflater.inflate(R.layout.dialog_register_code_auth,null);

        builder.setTitle(R.string.confirmInfo);

        builder.setMessage(R.string.messageResidentCodeAuth);

        builder.setView(content);

        textViewApartment = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewFullName = (TextView) content.findViewById(R.id.textViewFullNameDialog);
        textViewEmail = (TextView) content.findViewById(R.id.textViewEmailDialog);

        apartment = getArguments().getString("apartment");
        fullName = getArguments().getString("fullName");
        email = getArguments().getString("email");
        id = getArguments().getLong("id");

        textViewApartment.setText(apartment);
        textViewFullName.setText(fullName);
        textViewEmail.setText(email);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                UpdateCodeAuth codeAuth = new UpdateCodeAuth(id,getContext());
                codeAuth.execute();

                SearchResidentsFragment fragmentSearchResidents = new SearchResidentsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchResidents).commit();
                NavigationView navigationView = (NavigationView)getActivity().findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.searchResidents);
                Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                toolbar.setTitle(getString(R.string.searchResident));

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
