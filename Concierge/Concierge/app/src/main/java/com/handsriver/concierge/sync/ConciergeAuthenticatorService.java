package com.handsriver.concierge.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Created by alain_r._trouve_silva after 27-04-17.
 */

public class ConciergeAuthenticatorService extends Service {

    private ConciergeAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new ConciergeAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
