package com.handsriver.concierge.timekeeping;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.TimekeepingEntry;
import com.handsriver.concierge.database.ConciergeContract.PorterEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.utilities.BCrypt;
import com.handsriver.concierge.utilities.FormatICAO9303;
import com.handsriver.concierge.utilities.RutFormat;
import com.handsriver.concierge.utilities.Utility;

import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 21-03-17.
 */

public class ExitTimekeepingFragment extends Fragment {
    View rootView;
    TextInputEditText rutEditText;
    TextInputEditText passwordEditText;
    StringBuilder ocr;
    String INVALID = "INVALID";
    LinearLayout linearTimekeeping;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        ocr = new StringBuilder();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_timekeeping, container, false);

        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return isScannerOCR(v,keyCode,event);
            }

        });

        rutEditText = (TextInputEditText) rootView.findViewById(R.id.rutInput);
        passwordEditText = (TextInputEditText) rootView.findViewById(R.id.passwordInput);
        passwordEditText.addTextChangedListener(inputAutomaticDocument);
        rutEditText.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });
        passwordEditText.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });

        rutEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                rutEditText.removeTextChangedListener(this);
                RutFormat.rutFormatRealTime(s);
                rutEditText.addTextChangedListener(this);
            }
        });

        linearTimekeeping = (LinearLayout) rootView.findViewById(R.id.linearTimekeeping);
        linearTimekeeping.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.app_bar_menu_exit_timekeeping, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.registerExitSave) {
            String rut = rutEditText.getText().toString();
            String rutClear = rut.replace(".","").replace("-","");
            String password = passwordEditText.getText().toString();
            String porterVerifiedId = porterVerified(rutClear);
            boolean porterVerifiedPassword = porterVerifiedPassword(rut,password);

            if (RutFormat.checkRutDv(rutEditText.getText().toString())){
                if (porterVerifiedId != null){
                    if (porterVerifiedPassword){
                        if (porterVerifiedEntry(porterVerifiedId)){
                            Bundle args = new Bundle();
                            args.putString("rut",rutClear);

                            DialogExitTimekeeping dialog = new DialogExitTimekeeping();
                            dialog.setArguments(args);
                            dialog.show(getFragmentManager(), "dialog");
                        }
                        else
                        {
                            Toast.makeText(getActivity().getApplicationContext(), "No se encuentra una entrada para asociarla a esta salida", Toast.LENGTH_LONG).show();
                        }

                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(), "La contraseña no coincide con el RUT ingresado", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "No se encuentra registrado como empleado", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "Rut incorrecto", Toast.LENGTH_LONG).show();
            }


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private TextWatcher inputAutomaticDocument = new TextWatcher() {
        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            String string_scan = s.toString();
            if(!string_scan.equals("")){

                char ini = string_scan.charAt(0);
                if(ini == '_' || string_scan.contains("<")){

                    if(ini == '_'){
                        string_scan = string_scan.substring(1,string_scan.length());
                    }
                    String rut = FormatICAO9303.returnRut(string_scan);

                    if (rut == null) {
                        Toast.makeText(getActivity().getApplicationContext(), "Lectura Errónea, Favor Escanear Nuevamente", Toast.LENGTH_LONG).show();
                    } else if (rut.equals(INVALID)){
                        Toast.makeText(getActivity().getApplicationContext(), "Documento Inválido", Toast.LENGTH_LONG).show();
                    } else {
                        String porter_id = porterVerified(rut);
                        if (porter_id != null){
                            if (porterVerifiedEntry(porter_id)){
                                Bundle args = new Bundle();
                                args.putString("rut",rut);

                                DialogExitTimekeeping dialog = new DialogExitTimekeeping();
                                dialog.setArguments(args);
                                dialog.show(getFragmentManager(), "dialog");
                            }
                            else{
                                Toast.makeText(getActivity().getApplicationContext(), "No se encuentra una entrada para asociarla a esta salida", Toast.LENGTH_LONG).show();
                            }

                        } else{
                            Toast.makeText(getActivity().getApplicationContext(), "No se encuentra registrado como empleado", Toast.LENGTH_LONG).show();
                        }
                    }
                    passwordEditText.setText("");

                }

            }

        }
    };
    private boolean isScannerOCR(View v, int keycode, KeyEvent event){

        if (event.getDeviceId() == KeyCharacterMap.VIRTUAL_KEYBOARD) {
            return false;
        }
        else{
            if (event.getAction() == KeyEvent.ACTION_DOWN && keycode != KeyEvent.KEYCODE_SHIFT_LEFT)
            {
                ocr.append((char)event.getUnicodeChar());
                if (keycode == KeyEvent.KEYCODE_ENTER)
                {
                    String rut = FormatICAO9303.returnRut(ocr.toString());

                    if (rut == null) {
                        Toast.makeText(getActivity().getApplicationContext(), "Lectura Errónea, Favor Escanear Nuevamente", Toast.LENGTH_LONG).show();
                    } else if (rut.equals(INVALID)){
                        Toast.makeText(getActivity().getApplicationContext(), "Documento Inválido", Toast.LENGTH_LONG).show();
                    } else {
                        String porter_id = porterVerified(rut);
                        if (porter_id != null){
                            if (porterVerifiedEntry(porter_id)){
                                Bundle args = new Bundle();
                                args.putString("rut",rut);

                                DialogExitTimekeeping dialog = new DialogExitTimekeeping();
                                dialog.setArguments(args);
                                dialog.show(getFragmentManager(), "dialog");
                            }
                            else{
                                Toast.makeText(getActivity().getApplicationContext(), "No se encuentra una entrada para asociarla a esta salida", Toast.LENGTH_LONG).show();
                            }

                        } else{
                            Toast.makeText(getActivity().getApplicationContext(), "No se encuentra registrado como empleado", Toast.LENGTH_LONG).show();
                        }
                    }
                    ocr.setLength(0);
                }
            }
            return true;
        }
    }

    private String porterVerified(String rut){

        String[] projection = {
                PorterEntry.COLUMN_RUT,
                PorterEntry.COLUMN_PORTER_ID_SERVER,
                PorterEntry.COLUMN_ACTIVE
        };

        Cursor porters;
        try {
            SelectToDB selectPorters = new SelectToDB(PorterEntry.TABLE_NAME,projection,null,null,null,null,null,null);
            porters = selectPorters.execute().get();
        }catch (Exception e){
            porters = null;
        }

        if (porters != null) {
            while (porters.moveToNext()){
                String rutFormat = porters.getString(porters.getColumnIndex(PorterEntry.COLUMN_RUT)).replace(".","").replace("-","");
                String porterId = porters.getString(porters.getColumnIndex(PorterEntry.COLUMN_PORTER_ID_SERVER));
                Integer isActive = porters.getInt(porters.getColumnIndex(PorterEntry.COLUMN_ACTIVE));

                if (rut.equals(rutFormat) && isActive == 1){
                    return porterId;
                }
            }
        }

        return null;
    }

    private boolean porterVerifiedPassword(String rut, String password){

        String[] projection = {
                PorterEntry.COLUMN_RUT,
                PorterEntry.COLUMN_PASSWORD
        };
        String selection = PorterEntry.COLUMN_RUT + " = ? AND active = 1 ";
        String [] selectionArgs = {rut};

        Cursor porter;
        try {
            SelectToDB selectPorter = new SelectToDB(PorterEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);
            porter = selectPorter.execute().get();
        }catch (Exception e){
            porter = null;
        }

        if (porter != null){
            if (porter.moveToFirst()){
                if(BCrypt.checkpw(password,porter.getString(porter.getColumnIndex(PorterEntry.COLUMN_PASSWORD)))){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean porterVerifiedEntry(String porterId){

        String[] projection = {
                TimekeepingEntry._ID
        };
        String selection = TimekeepingEntry.COLUMN_PORTER_ID + " = ? AND " + TimekeepingEntry.COLUMN_EXIT_PORTER  + " IS NULL AND " + TimekeepingEntry.COLUMN_ENTRY_PORTER + " = (SELECT MAX (" + TimekeepingEntry.COLUMN_ENTRY_PORTER + ") FROM " + TimekeepingEntry.TABLE_NAME + " WHERE " + TimekeepingEntry.COLUMN_PORTER_ID + " = ? )";
        String [] selectionArgs = {porterId,porterId};

        Cursor porter;
        try {
            SelectToDB selectTimekeeping = new SelectToDB(TimekeepingEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);
            porter = selectTimekeeping.execute().get();
        }catch (Exception e){
            porter = null;
        }

        if (porter != null) {
            if (porter.moveToFirst()){
                return true;
            }
        }
        return false;
    }
}
