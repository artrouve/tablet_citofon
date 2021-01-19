package com.handsriver.concierge.commonspaces;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.CommonspaceVisitsEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.suppliers.DialogExitSuppliersRegister;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 17-03-17.
 */

public class DetailSearchCommonspacesvisitsListFragment extends Fragment {
    View rootView;
    TextView textViewNameCommonspace;
    TextView textViewNameCommonspaceVisit;
    TextView textViewApartmentCommonspaceVisit;
    TextView textViewDocumentNumberCommonspaceVisit;
    TextView textViewEntry;
    TextView textViewExitVisit;

    String idCommonspacevisit;
    String nameCommonspace;
    String nameCommonspacevisit;
    String documentnumberCommonspacevisit;
    String apartmentnumberCommonspacevisit;
    String entry;
    String exitCommonspaceVisit;
    Button buttonExitCommonspaceVisit;


    public static final String NO_AVAILABLE = "Sin Información";
    public static final int DIALOG_FRAGMENT = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();

        idCommonspacevisit = args.getString("id");
        nameCommonspace = args.getString("commonspace");
        nameCommonspacevisit = args.getString("name");
        documentnumberCommonspacevisit = args.getString("document_number");
        entry = args.getString("entry");
        exitCommonspaceVisit = args.getString("exitDate");
        apartmentnumberCommonspacevisit  = args.getString("apartmentNumber");

        rootView = inflater.inflate(R.layout.detail_commonspacevisits, container, false);

        buttonExitCommonspaceVisit = (Button) rootView.findViewById(R.id.buttonExitVisit);



        buttonExitCommonspaceVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("visitId",idCommonspacevisit);
                DialogExitCommonspacesvisitsRegister dialog = new DialogExitCommonspacesvisitsRegister();
                dialog.setArguments(args);
                dialog.setTargetFragment(DetailSearchCommonspacesvisitsListFragment.this,0);
                dialog.show(getFragmentManager(), "dialog");
            }
        });
        getExitDate();
        if(exitCommonspaceVisit == null){
            exitCommonspaceVisit = NO_AVAILABLE;
        }

        if (exitCommonspaceVisit.equals(NO_AVAILABLE))
        {
            buttonExitCommonspaceVisit.setVisibility(View.VISIBLE);
        }

        textViewEntry = (TextView) rootView.findViewById(R.id.textViewDetailEntry);
        textViewEntry.setText(entry);


        textViewNameCommonspace = (TextView) rootView.findViewById(R.id.textViewDetailCommonspaceName);
        textViewNameCommonspace.setText(nameCommonspace);

        textViewNameCommonspaceVisit = (TextView) rootView.findViewById(R.id.textViewDetailfullName);
        if(nameCommonspacevisit == null){
            textViewNameCommonspaceVisit.setText(NO_AVAILABLE);
        }
        else{
            textViewNameCommonspaceVisit.setText(nameCommonspacevisit);
        }



        textViewDocumentNumberCommonspaceVisit = (TextView) rootView.findViewById(R.id.textViewDetailDocumentNumber);
        if(documentnumberCommonspacevisit == null){
            textViewDocumentNumberCommonspaceVisit.setText(NO_AVAILABLE);
        }
        else{
            textViewDocumentNumberCommonspaceVisit.setText(documentnumberCommonspacevisit);
        }


        textViewApartmentCommonspaceVisit = (TextView) rootView.findViewById(R.id.textViewDetailApartmentNumber);
        textViewApartmentCommonspaceVisit.setText(apartmentnumberCommonspacevisit);

        textViewExitVisit = (TextView) rootView.findViewById(R.id.textViewDetailExit);
        textViewExitVisit.setText(exitCommonspaceVisit);




        return rootView;
    }

    private boolean isScannerOCR(View v, int keycode, KeyEvent event){

        if (event.getDeviceId() == KeyCharacterMap.VIRTUAL_KEYBOARD) {
            return false;
        }
        return true;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK){
                    getExitDate();
                    textViewExitVisit.setText(exitCommonspaceVisit);

                    if (!exitCommonspaceVisit.equals(NO_AVAILABLE))
                    {

                        buttonExitCommonspaceVisit.setVisibility(View.GONE);
                    }
                }
                break;
        }
    }

    public void getExitDate (){

        final String query1 = "SELECT " + CommonspaceVisitsEntry.COLUMN_EXIT_VISIT +
                " FROM " + CommonspaceVisitsEntry.TABLE_NAME +
                " WHERE " + CommonspaceVisitsEntry._ID + " = ?";

        String [] args2 = new String[]{idCommonspacevisit};

        Cursor c1;

        try {
            SelectToDBRaw selectExitCommonspaceVisit = new SelectToDBRaw(query1,args2);
            c1 = selectExitCommonspaceVisit.execute().get();
        }catch (Exception e){
            c1 = null;
        }

        if (c1 != null){
            if(c1.moveToFirst()){
                if (c1.getString(0) == null || c1.getString(0).isEmpty())
                {
                    exitCommonspaceVisit = NO_AVAILABLE;
                }
                else
                {
                    exitCommonspaceVisit = Utility.changeDateFormat(c1.getString(0),"ENTRY");
                }


            }
        }
    }
}
