package afelion.android.switchy.observer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import afelion.android.switchy.R;
import afelion.android.switchy.WidgetContainer;

public class BrightnessObserver extends BaseSettingObserver {
    private static final String TAG = "BrightnessObserver";

    public BrightnessObserver(WidgetContainer widgetContainer) {
        super(widgetContainer);
    }

    @Override
    public int getButtonId() {
        return R.id.button_brightness;
    }

    @Override
    public void updateView(Context context, RemoteViews views) {
        int color = R.color.state_unknown;

        try {
            int brightnessMode = getSystemBrightnessMode(context);
            if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                color = R.color.state_enabled;
            } else {
                color = R.color.state_disabled;
            }
        } catch (Exception e) {
            Log.d(TAG, "brightness: ", e);
        }
        views.setInt(getButtonId(), "setBackgroundResource", color);
    }

    @Override
    public void toggleState(Context context) {
        try {
            int brightnessMode = getSystemBrightnessMode(context);

            if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                setSystemBrightnessMode(context, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                setSystemBrightness(context, 0);
            } else {
                // Change to auto mode
                setSystemBrightnessMode(context, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            }
        } catch (Exception e) {
            Log.d(TAG, "brightness: ", e);
        }
    }

    @Override
    protected String[] getSettingsName() {
        return new String[] {
                Settings.System.SCREEN_BRIGHTNESS,
                Settings.System.SCREEN_BRIGHTNESS_MODE
        };
    }

    private static void setSystemBrightness(Context context, int brightness) throws Exception {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    private static int getSystemBrightnessMode(Context context) throws Exception {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE);
    }

    private static void setSystemBrightnessMode(Context context, int brightnessMode)
            throws Exception {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, brightnessMode);
    }
}
