package com.handsriver.concierge.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

/**
 * Created by Created by alain_r._trouve_silva after 27-04-17.
 */

public class UpdateGooglePlayServicesSyncService extends Service {

    private static UpdateGooglePlayServicesSyncAdapter sUpdateGooglePlayServicesSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("UpdPlayServSyncAdapter", "onCreate - UpdateGooglePlayServicesAdapter");
        if(sUpdateGooglePlayServicesSyncAdapter == null){
            sUpdateGooglePlayServicesSyncAdapter = new UpdateGooglePlayServicesSyncAdapter(getApplicationContext(),true);
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sUpdateGooglePlayServicesSyncAdapter.getSyncAdapterBinder();
    }
}
