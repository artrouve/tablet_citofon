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

public class ParcelTypeSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static ParcelTypeSyncAdapter sParcelTypeSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("ParcelTypeSyncService", "onCreate - ParcelTypeSyncService");
        ConciergeDbHelper helper= new ConciergeDbHelper(getApplicationContext());
        DatabaseManager.initializeInstance(helper);
        synchronized (sSyncAdapterLock){
            if(sParcelTypeSyncAdapter == null){
                sParcelTypeSyncAdapter = new ParcelTypeSyncAdapter(getApplicationContext(),true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sParcelTypeSyncAdapter.getSyncAdapterBinder();
    }
}
