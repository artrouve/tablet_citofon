package com.handsriver.concierge.database.insertsTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.PaymentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class InsertPayments extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String nullColumnHack;
    private String documentNumber;
    private String documentNumberBuilding;
    private String paymentType;
    private String amount;
    private String entry;
    private int apartmentId;
    private int gatewayId;
    private int porterId;
    private Context mContext;


    private static final String TAG = "InsertParcels";
    private static final int IS_SYNC = 0;


    public InsertPayments(String nullColumnHack, String documentNumber, String documentNumberBuilding,String paymentType, String amount,String entry, int apartmentId, int gatewayId, int porterId, Context mContext){
        this.nullColumnHack = nullColumnHack;
        this.documentNumber = documentNumber;
        this.documentNumberBuilding = documentNumberBuilding;
        this.paymentType = paymentType;
        this.amount = amount;
        this.entry = entry;
        this.apartmentId = apartmentId;
        this.gatewayId = gatewayId;
        this.porterId = porterId;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = PaymentEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(PaymentEntry.COLUMN_APARTMENT_ID,apartmentId);
            if (documentNumber.length() > 0){
                values.put(PaymentEntry.COLUMN_NUMBER_TRX,documentNumber);

            }
            if (documentNumberBuilding.length() > 0){
                values.put(PaymentEntry.COLUMN_NUMBER_RECEIPT,documentNumberBuilding);
            }
            values.put(PaymentEntry.COLUMN_PAYMENT_TYPE,paymentType);
            values.put(PaymentEntry.COLUMN_AMOUNT,amount);
            values.put(PaymentEntry.COLUMN_DATE_REGISTER,entry);
            values.put(PaymentEntry.COLUMN_GATEWAY_ID,gatewayId);
            values.put(PaymentEntry.COLUMN_IS_SYNC,IS_SYNC);
            values.put(PaymentEntry.COLUMN_PORTER_ID,porterId);

            db.insert(tableName,nullColumnHack,values);

            ConfigureSyncAccount.syncImmediatelyPayments(mContext);
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
