package afelion.android.switchy.observer;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import afelion.android.switchy.R;
import afelion.android.switchy.WidgetContainer;

public class RotationSensorObserver extends BaseSettingObserver {
    private static String TAG = "RotationSensorObserver";

    public RotationSensorObserver(WidgetContainer widgetContainer) {
        super(widgetContainer);
    }

    @Override
    public int getButtonId() {
        return R.id.button_rotation;
    }

    @Override
    public void updateView(Context context, RemoteViews views) {
        int color = R.color.state_unknown;

        try {
            int enabled = getRotationSensor(context);
            color = (enabled == 0) ? R.color.state_disabled : R.color.state_enabled;
        } catch (Exception e) {
            Log.e(TAG, "rotation: ", e);
        }

        views.setInt(getButtonId(), "setBackgroundResource", color);
    }

    @Override
    public void toggleState(Context context) {
        try {
            int enabled = getRotationSensor(context);
            if (enabled == 0) {
                setRotationSensor(context, 1);
            } else {
                setRotationSensor(context, 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "rotation: ", e);
        }
    }

    @Override
    protected String[] getSettingsName() {
        return new String[] {
                Settings.System.ACCELEROMETER_ROTATION
        };
    }

    private static int getRotationSensor(Context context) throws Exception {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION);
    }

    private static void setRotationSensor(Context context, int rotation) throws Exception {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, rotation);
    }
}
