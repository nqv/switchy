package afelion.android.switchy.observer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import afelion.android.switchy.StateObserver;
import afelion.android.switchy.WidgetContainer;

public abstract class BaseSettingObserver implements StateObserver {
    private static final String TAG = "BaseSettingObserver";
    private final WidgetContainer widgetContainer;
    private SettingsObserver settingsObserver = null;

    public BaseSettingObserver(WidgetContainer widgetContainer) {
        this.widgetContainer = widgetContainer;
    }

    @Override
    public String[] getIntentActions() {
        return null;
    }

    @Override
    public void onEnabled(Context context) {
        if (settingsObserver == null) {
            settingsObserver = new SettingsObserver(new Handler(), context);
        }
        ContentResolver resolver = context.getContentResolver();
        for (String name : getSettingsName()) {
            resolver.registerContentObserver(Settings.System.getUriFor(name), false,
                    settingsObserver);
        }
    }

    @Override
    public void onDisabled(Context context) {
        if (settingsObserver != null) {
            context.getContentResolver().unregisterContentObserver(settingsObserver);
            settingsObserver = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        widgetContainer.updateWidget(context);
    }

    protected abstract String[] getSettingsName();

    protected class SettingsObserver extends ContentObserver {
        private final Context context;

        public SettingsObserver(Handler handler, Context context) {
            super(handler);
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            onReceive(context, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG, "Setting changed: " + uri.toString());
            onReceive(context, null);
        }
    }
}
