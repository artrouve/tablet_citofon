package com.handsriver.concierge.timekeeping;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;

import com.handsriver.concierge.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by Created by alain_r._trouve_silva after 04-04-17.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    DatePickerDialog dialog;
    Boolean isClickOK = false;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        dialog = new DatePickerDialog(getActivity(),this,year,month,day);
        dialog.setTitle("");
        dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Buscar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isClickOK = true;
            }
        });
        dialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isClickOK = false;
            }
        });

        dialog.getDatePicker().setCalendarViewShown(false);


        try{
            dialog.getDatePicker().findViewById(Resources.getSystem().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
        }
        catch (Exception e){

        }

        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        if (isClickOK){
            SearchView searchTimekeeping;
            DateFormatSymbols format = new DateFormatSymbols();

            searchTimekeeping = (SearchView) getActivity().findViewById(R.id.searchTimekeeping);
            searchTimekeeping.setQuery(format.getMonths()[month] + " " + String.valueOf(year),false);
        }
    }
}
