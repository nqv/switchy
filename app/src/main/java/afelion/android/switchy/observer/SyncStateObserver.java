package afelion.android.switchy.observer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import afelion.android.switchy.Switchy;
import afelion.android.switchy.R;

public class SyncStateObserver extends BaseStateObserver {
    // {@link android.content.ContentResolver#ACTION_SYNC_CONN_STATUS_CHANGED}
    private static final String ACTION_SYNC_CONN_STATUS_CHANGED =
            "com.android.sync.SYNC_CONN_STATUS_CHANGED";

    @Override
    public int getButtonId() {
        return R.id.button_sync;
    }

    @Override
    public String[] getIntentActions() {
        return new String[] { ACTION_SYNC_CONN_STATUS_CHANGED };
    }

    /**
     * Receive {@link android.content.ContentResolver#ACTION_SYNC_CONN_STATUS_CHANGED}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        setCurrentState(context, getCurrentState(context));
    }

    @Override
    protected int getCurrentState(Context context) {
        boolean enabled = ContentResolver.getMasterSyncAutomatically();
        return enabled ? Switchy.STATE_ENABLED : Switchy.STATE_DISABLED;
    }

    @Override
    protected void requestStateChange(final Context context, final boolean on) {
        final boolean syncEnabled = ContentResolver.getMasterSyncAutomatically();
        if (syncEnabled == on) {
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... args) {
                // Turning sync on/off
                ContentResolver.setMasterSyncAutomatically(on);
                return null;
            }
        }.execute();
    }
}
