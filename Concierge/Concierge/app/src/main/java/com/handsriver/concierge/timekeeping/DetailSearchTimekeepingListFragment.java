package com.handsriver.concierge.timekeeping;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.utilities.Utility;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Created by alain_r._trouve_silva after 06-04-17.
 */

public class DetailSearchTimekeepingListFragment extends Fragment {
    View rootView;
    TextView textViewFullName;
    TextView textViewExitPorter;
    TextView textViewEntryPorter;
    TextView textViewDifferent;
    TextView textViewRut;
    String firstName;
    String lastName;
    String exitPorter;
    String entryPorter;
    String rut;
    String fullName;
    String differentExitEntry;

    public static final String NO_AVAILABLE = "Sin Información";
    ParsePosition position = new ParsePosition(0);
    ParsePosition position2 = new ParsePosition(0);




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();

        firstName = args.getString("firstName",NO_AVAILABLE);
        lastName = args.getString("lastName",NO_AVAILABLE);
        fullName = firstName + " " +lastName;
        rut = args.getString("rut",NO_AVAILABLE);
        entryPorter = Utility.changeDateFormat(args.getString("entryPorter"),"ENTRY");
        exitPorter = args.getString("exitPorter");

        if (exitPorter == null || exitPorter.isEmpty())
        {
            exitPorter = NO_AVAILABLE;
            differentExitEntry = NO_AVAILABLE;
        }
        else
        {
            exitPorter = Utility.changeDateFormat(exitPorter,"ENTRY");

            differenceDate(exitPorter,entryPorter);

        }

        rootView = inflater.inflate(R.layout.detail_timekeeping, container, false);

        textViewFullName = (TextView) rootView.findViewById(R.id.textViewDetailFullNameTimekeeping);
        textViewFullName.setText(fullName);
        textViewRut = (TextView) rootView.findViewById(R.id.textViewDetailRutTimekeeping);
        textViewRut.setText(rut);
        textViewEntryPorter = (TextView) rootView.findViewById(R.id.textViewDetailEntryPorter);
        textViewEntryPorter.setText(entryPorter);
        textViewExitPorter = (TextView) rootView.findViewById(R.id.textViewDetailExitPorter);
        textViewExitPorter.setText(exitPorter);
        textViewDifferent = (TextView) rootView.findViewById(R.id.textViewDetailDifferent);
        textViewDifferent.setText(differentExitEntry);

        return rootView;
    }

    private void differenceDate (String exitPorter, String entryPorter){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date dateEntry = simpleDateFormat.parse(entryPorter,position);
        Date dateExit = simpleDateFormat.parse(exitPorter,position2);

        long different =  dateExit.getTime() - dateEntry.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;

        differentExitEntry = String.valueOf(elapsedHours)+ " horas " + String.valueOf(elapsedMinutes) + " minutos";
    }
}
