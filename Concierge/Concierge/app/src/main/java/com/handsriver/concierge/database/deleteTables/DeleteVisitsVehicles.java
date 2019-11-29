package com.handsriver.concierge.database.deleteTables;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.VisitEntry;
import com.handsriver.concierge.database.ConciergeContract.VehicleEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.utilities.Utility;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class DeleteVisitsVehicles{
    private static final String TAG = "DeleteTimekeeping";
    private static final String IS_SYNC = "1";
    private static final String IS_UPDATE = "1";

    public static void run(Context mContext) {
        SQLiteDatabase db;
        Boolean isMarkExit;
        db = DatabaseManager.getInstance().openDatabase();

        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        isMarkExit = settingsPrefs.getBoolean(mContext.getResources().getString(R.string.pref_id_mark_exit_key),true);

        try {

            String whereClauseVisit = VisitEntry.COLUMN_IS_SYNC + " = ? AND " + VisitEntry.COLUMN_ENTRY +  " < '" + Utility.getDateSimpleForServer7Days()+"'";
            String [] whereArgsVisit = {IS_SYNC};
            db.delete(VisitEntry.TABLE_NAME,whereClauseVisit,whereArgsVisit);

            if (isMarkExit){
                String whereClauseVehicle = VehicleEntry.COLUMN_IS_SYNC + " = ? AND " + VehicleEntry.COLUMN_IS_UPDATE + " = ? AND " + VehicleEntry.COLUMN_ENTRY + " < '" + Utility.getDateSimpleForServer7Days()+"'";
                String [] whereArgsVehicle = {IS_SYNC,IS_UPDATE};
                db.delete(VehicleEntry.TABLE_NAME,whereClauseVehicle,whereArgsVehicle);

            }
            else{
                String whereClauseVehicle = VehicleEntry.COLUMN_IS_SYNC + " = ? AND " + VehicleEntry.COLUMN_ENTRY + " < '" + Utility.getDateSimpleForServer7Days()+"'";
                String [] whereArgsVehicle = {IS_SYNC};
                db.delete(VehicleEntry.TABLE_NAME,whereClauseVehicle,whereArgsVehicle);
            }

        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
