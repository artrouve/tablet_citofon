package com.handsriver.concierge.database.insertsTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;
import com.handsriver.concierge.visits.Visit;
import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.ConciergeDbHelper;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class InsertSuppliers extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String nullColumnHack;
    private ArrayList<Visit> visitList;
    private String entry;
    private int gatewayId;
    private int porterId;
    private int supplierId;
    private String licensePlate;
    private Context mContext;


    private static final String TAG = "InsertSuppliers";
    private static final int IS_SYNC = 0;
    private static final int IS_UPDATE = 0;


    public InsertSuppliers(String nullColumnHack, ArrayList<Visit> visitList, String entry, int gatewayId, int porterId, String licensePlate, int supplierId, Context mContext){
        this.nullColumnHack = nullColumnHack;
        this.visitList = visitList;
        this.entry = entry;
        this.gatewayId = gatewayId;
        this.porterId = porterId;
        this.licensePlate = licensePlate;
        this.supplierId = supplierId;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = SupplierVisitsEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            for (Visit visit:visitList){
                values.put(SupplierVisitsEntry.COLUMN_FULL_NAME,visit.getFullName());
                values.put(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER,visit.getDocumentNumber());
                values.put(SupplierVisitsEntry.COLUMN_BIRTHDATE,visit.getBirthdate());
                values.put(SupplierVisitsEntry.COLUMN_GENDER,visit.getGender());
                values.put(SupplierVisitsEntry.COLUMN_NATIONALITY,visit.getNationality());
                values.put(SupplierVisitsEntry.COLUMN_ENTRY,entry);
                values.put(SupplierVisitsEntry.COLUMN_ENTRY_PORTER_ID, porterId);
                values.put(SupplierVisitsEntry.COLUMN_GATEWAY_ID,gatewayId);
                values.put(SupplierVisitsEntry.COLUMN_SUPPLIER_ID,supplierId);
                if (!licensePlate.isEmpty()){
                    values.put(SupplierVisitsEntry.COLUMN_LICENSE_PLATE, licensePlate);
                }
                values.put(SupplierVisitsEntry.COLUMN_IS_SYNC,IS_SYNC);
                values.put(SupplierVisitsEntry.COLUMN_IS_UPDATE,IS_UPDATE);

                db.insert(tableName,nullColumnHack,values);

                ConfigureSyncAccount.syncImmediatelyVisitsSuppliers(mContext);
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
