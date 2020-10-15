package com.handsriver.concierge.parcels;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.home.HomeFragment;

/**
 * Created by Created by alain_r._trouve_silva after 20-04-17.
 */

public class DialogReturnIdParcelRegister extends DialogFragment {
    TextView textViewUniqueid;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String unique_id = getArguments().getString("uniqueId");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_register_parcels_unique,null);

        builder.setTitle(R.string.confirmInfo);
        builder.setMessage(R.string.textUniqueParcels);
        builder.setView(content);

        textViewUniqueid = (TextView) content.findViewById(R.id.textViewUniqueId);
        textViewUniqueid.setText(unique_id);

        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HomeFragment fragmentHome = new HomeFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentHome).commit();
                NavigationView navigationView = (NavigationView)getActivity().findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.home);
                Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                toolbar.setTitle(getString(R.string.app_name));
            }
        });

        return builder.create();

    }
}
