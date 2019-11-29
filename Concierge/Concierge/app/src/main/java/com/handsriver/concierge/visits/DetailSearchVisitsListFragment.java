package com.handsriver.concierge.visits;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.utilities.Utility;

/**
 * Created by Created by alain_r._trouve_silva after 06-03-17.
 */

public class DetailSearchVisitsListFragment extends Fragment {

    View rootView;
    TextView textViewFullname;
    TextView textViewDocumentNumber;
    TextView textViewGender;
    TextView textViewBirthdate;
    TextView textViewNationality;
    TextView textViewEntry;
    TextView textViewApartmentNumber;
    String fullname;
    String documentNumber;
    String gender;
    String birthdate;
    String nationality;
    String entry;
    String apartmentNumber;

    public static final String NO_AVAILABLE = "Sin Información";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();

        fullname = args.getString("fullname",NO_AVAILABLE);
        documentNumber = args.getString("documentNumber",NO_AVAILABLE);
        nationality = args.getString("nationality",NO_AVAILABLE);
        gender = args.getString("gender",NO_AVAILABLE);
        entry = Utility.changeDateFormat(args.getString("entry"),"ENTRY");
        birthdate = args.getString("birthdate");
        apartmentNumber = args.getString("apartmentNumber",NO_AVAILABLE);

        if (birthdate == null || birthdate.isEmpty())
        {
            birthdate = NO_AVAILABLE;
        }
        else
        {
            birthdate = Utility.changeDateFormat(birthdate,"BIRTHDATE");
        }

        rootView = inflater.inflate(R.layout.detail_visits, container, false);

        textViewEntry = (TextView) rootView.findViewById(R.id.textViewDetailEntry);
        textViewEntry.setText(entry);
        textViewBirthdate = (TextView) rootView.findViewById(R.id.textViewDetailBirthdate);
        textViewBirthdate.setText(birthdate);
        textViewDocumentNumber = (TextView) rootView.findViewById(R.id.textViewDetailDocumentNumber);
        textViewDocumentNumber.setText(documentNumber);
        textViewFullname = (TextView) rootView.findViewById(R.id.textViewDetailfullName);
        textViewFullname.setText(fullname);
        textViewNationality = (TextView) rootView.findViewById(R.id.textViewDetailNationality);
        textViewNationality.setText(nationality);
        textViewGender = (TextView) rootView.findViewById(R.id.textViewDetailGender);
        textViewGender.setText(gender);
        textViewApartmentNumber = (TextView) rootView.findViewById(R.id.textViewDetailApartmentNumber);
        textViewApartmentNumber.setText(apartmentNumber);

        return rootView;
    }
}
