package afelion.android.switchy;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import afelion.android.switchy.observer.BluetoothStateObserver;
import afelion.android.switchy.observer.BrightnessObserver;
import afelion.android.switchy.observer.LocationStateObserver;
import afelion.android.switchy.observer.NetworkStateObserver;
import afelion.android.switchy.observer.RotationSensorObserver;
import afelion.android.switchy.observer.SettingsLauncher;
import afelion.android.switchy.observer.SyncStateObserver;
import afelion.android.switchy.observer.WifiStateObserver;

public class SwitchyWidgetProvider extends AppWidgetProvider implements WidgetContainer {
    private static final ComponentName WIDGET_COMPONENT_NAME = new ComponentName(
            "afelion.android.switchy",
            "afelion.android.switchy.SwitchyWidgetProvider");

    // buttonId is used as identifier
    private StateObserverList stateObservers;

    public SwitchyWidgetProvider() {
        super();

        // Initialize state observers
        stateObservers = new StateObserverList();
        stateObservers.addObserver(new WifiStateObserver());
        stateObservers.addObserver(new NetworkStateObserver());
        stateObservers.addObserver(new BluetoothStateObserver());
        stateObservers.addObserver(new LocationStateObserver());
        stateObservers.addObserver(new SyncStateObserver());
        stateObservers.addObserver(new BrightnessObserver(this));
        stateObservers.addObserver(new RotationSensorObserver(this));
        stateObservers.addObserver(new SettingsLauncher());
    }

    /**
     * Called for every update of the widget.
     * @param appWidgetIds Contains the ids of appWidgetIds for which an update is needed
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each requested appWidgetId
        RemoteViews updateViews = buildUpdate(context);

        // Tell the AppWidgetManager to perform an update on the current app widget
        for (int id : appWidgetIds) {
            appWidgetManager.updateAppWidget(id, updateViews);
        }
    }

    /**
     * Called the first time an instance of your widget is added to the homescreen.
     */
    @Override
    public void onEnabled(Context context) {
        for (StateObserver observer : stateObservers.getObservers()) {
            observer.onEnabled(context);
        }
    }

    /**
     * Called once the last instance of your widget is removed from the homescreen.
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        for (StateObserver observer : stateObservers.getObservers()) {
            observer.onDisabled(context);
        }
        stateObservers.clear();
    }

    /**
     * Widget instance is removed from the homescreen.
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    }

    /**
     * Receives and processes a button pressed intent or state change.
     *
     * @param context
     * @param intent  Indicates the pressed button.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        boolean updateNeeded = false;
        StateObserver observer;

        observer = stateObservers.getByIntentAction(intent.getAction());
        if (observer != null) {
            observer.onReceive(context, intent);
            updateNeeded = true;
        } else if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE) && intent.getData() != null) {
            // Received button click event
            int buttonId = Integer.parseInt(intent.getData().getSchemeSpecificPart());
            observer = stateObservers.getByButtonId(buttonId);
            if (observer != null) {
                observer.toggleState(context);
                updateNeeded = true;
            }
        }
        if (updateNeeded) {
            updateWidget(context);
        }
    }

    /**
     * Updates the widget when something changes, or when a button is pushed.
     */
    @Override
    public void updateWidget(Context context) {
        RemoteViews views = buildUpdate(context);

        // Update specific list of appWidgetIds if given, otherwise default to all
        final AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (manager != null) {
            manager.updateAppWidget(WIDGET_COMPONENT_NAME, views);
        }
    }

    /**
     * Load image for given widget and build {@link RemoteViews} for it.
     */
    private RemoteViews buildUpdate(Context context) {
        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        for (StateObserver observer : stateObservers.getObservers()) {
            views.setOnClickPendingIntent(observer.getButtonId(),
                    createPendingIntent(context, observer.getButtonId()));
            // Updates buttons based on states
            observer.updateView(context, views);
        }
        return views;
    }

    /**
     * Creates PendingIntent to notify the widget of a button click.
     */
    private static PendingIntent createPendingIntent(Context context, int buttonId) {
        Intent intent = new Intent();

        intent.setClass(context, SwitchyWidgetProvider.class);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setData(Uri.parse("custom:" + buttonId));

        return PendingIntent.getBroadcast(
                context,
                0 /* requestCode */,
                intent,
                0 /* flags */);
    }

}
