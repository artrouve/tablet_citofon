package com.handsriver.concierge.database.insertsTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.CommonspaceVisitsEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;
import com.handsriver.concierge.visits.Visit;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class InsertCommonspacevisits extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String nullColumnHack;
    private ArrayList<Visit> visitList;
    private String entry;
    private int gatewayId;
    private int porterId;
    private int commonspaceId;
    private int apartmentId;
    private String licensePlate;
    private Context mContext;


    private static final String TAG = "InsertCommonspacesVisits";
    private static final int IS_SYNC = 0;
    private static final int IS_UPDATE = 0;


    public InsertCommonspacevisits(String nullColumnHack, ArrayList<Visit> visitList, String entry, int gatewayId, int porterId, String licensePlate, int commonspaceId,int apartmentId ,Context mContext){
        this.nullColumnHack = nullColumnHack;
        this.visitList = visitList;
        this.entry = entry;
        this.gatewayId = gatewayId;
        this.porterId = porterId;
        this.licensePlate = licensePlate;
        this.commonspaceId = commonspaceId;
        this.apartmentId = apartmentId;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = CommonspaceVisitsEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            for (Visit visit:visitList){
                values.put(CommonspaceVisitsEntry.COLUMN_FULL_NAME,visit.getFullName());
                values.put(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER,visit.getDocumentNumber());
                values.put(CommonspaceVisitsEntry.COLUMN_BIRTHDATE,visit.getBirthdate());
                values.put(CommonspaceVisitsEntry.COLUMN_GENDER,visit.getGender());
                values.put(CommonspaceVisitsEntry.COLUMN_NATIONALITY,visit.getNationality());
                values.put(CommonspaceVisitsEntry.COLUMN_ENTRY,entry);
                values.put(CommonspaceVisitsEntry.COLUMN_ENTRY_PORTER_ID, porterId);
                values.put(CommonspaceVisitsEntry.COLUMN_GATEWAY_ID,gatewayId);
                values.put(CommonspaceVisitsEntry.COLUMN_APARTMENT_ID,apartmentId);
                values.put(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID,commonspaceId);
                if (!licensePlate.isEmpty()){
                    values.put(CommonspaceVisitsEntry.COLUMN_LICENSE_PLATE, licensePlate);
                }
                values.put(CommonspaceVisitsEntry.COLUMN_IS_SYNC,IS_SYNC);
                values.put(CommonspaceVisitsEntry.COLUMN_IS_UPDATE,IS_UPDATE);

                db.insert(tableName,nullColumnHack,values);

            }
            ConfigureSyncAccount.syncImmediatelyVisitsCommonspaces(mContext);

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
