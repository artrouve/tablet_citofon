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

public class PaymentsDeleteSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static PaymentsDeleteSyncAdapter sPaymentsDeleteSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("PaymentsDelSyncService", "onCreate - PaymentsDeleteSyncService");
        ConciergeDbHelper helper= new ConciergeDbHelper(getApplicationContext());
        DatabaseManager.initializeInstance(helper);
        synchronized (sSyncAdapterLock){
            if(sPaymentsDeleteSyncAdapter == null){
                sPaymentsDeleteSyncAdapter = new PaymentsDeleteSyncAdapter(getApplicationContext(),true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sPaymentsDeleteSyncAdapter.getSyncAdapterBinder();
    }
}
