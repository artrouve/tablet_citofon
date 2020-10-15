package com.handsriver.concierge.database.insertsTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.visits.Visit;
import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.ConciergeDbHelper;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class InsertVisits extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String nullColumnHack;
    private ArrayList<Visit> visitList;
    private String entry;
    private int apartmentId;
    private int gatewayId;
    private int porterId;


    private static final String TAG = "InsertVisits";
    private static final int IS_SYNC = 0;
    private static final int IS_UPDATE = 0;


    public InsertVisits(String nullColumnHack, ArrayList<Visit> visitList, String entry, int apartmentId, int gatewayId, int porterId){
        this.nullColumnHack = nullColumnHack;
        this.visitList = visitList;
        this.entry = entry;
        this.apartmentId = apartmentId;
        this.gatewayId = gatewayId;
        this.porterId = porterId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = VisitEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();

        try {
            ContentValues values = new ContentValues();
            for (Visit visit:visitList){
                values.put(VisitEntry.COLUMN_FULL_NAME,visit.getFullName());
                values.put(VisitEntry.COLUMN_DOCUMENT_NUMBER,visit.getDocumentNumber());
                values.put(VisitEntry.COLUMN_BIRTHDATE,visit.getBirthdate());
                values.put(VisitEntry.COLUMN_OPTIONAL,visit.getOptional());
                values.put(VisitEntry.COLUMN_GENDER,visit.getGender());
                values.put(VisitEntry.COLUMN_NATIONALITY,visit.getNationality());
                values.put(VisitEntry.COLUMN_ENTRY,entry);
                values.put(VisitEntry.COLUMN_PORTER_ID, porterId);
                values.put(VisitEntry.COLUMN_GATEWAY_ID,gatewayId);
                values.put(VisitEntry.COLUMN_APARTMENT_ID, apartmentId);
                values.put(VisitEntry.COLUMN_IS_SYNC,IS_SYNC);
                values.put(VisitEntry.COLUMN_IS_UPDATE,IS_UPDATE);

                db.insert(tableName,nullColumnHack,values);
            }
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        DatabaseManager.getInstance().closeDatabase();
        super.onPostExecute(aVoid);
    }
}
