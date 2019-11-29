package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.PorterEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;
import com.handsriver.concierge.utilities.BCrypt;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class UpdatePorters extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private int id;
    private Context mContext;
    private String password;


    private static final String TAG = "UpdatePorterPassword";
    private static final String IS_UPDATE = "1";


    public UpdatePorters(int id, Context mContext,String password){
        this.id = id;
        this.mContext = mContext;
        this.password = password;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = PorterEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(PorterEntry.COLUMN_PASSWORD, BCrypt.hashpw(password,BCrypt.gensalt(10)));
            values.put(PorterEntry.COLUMN_IS_UPDATE_PASSWORD, IS_UPDATE);

            String whereClause = PorterEntry.COLUMN_PORTER_ID_SERVER + " = ? ";
            String [] whereArgs = {String.valueOf(id)};

            db.update(tableName,values,whereClause,whereArgs);

            ConfigureSyncAccount.syncImmediatelyPortersTablet(mContext);

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
