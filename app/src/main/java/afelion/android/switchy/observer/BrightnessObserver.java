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
    private static final int BRIGHTNESS_SCALE = 255;

    private static int[] brightnessLevels = null;

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
        String methodName = "setBackgroundResource";

        if (brightnessLevels == null) {
            brightnessLevels = getDefinedBrightnessLevels(context);
        }
        try {
            int brightnessMode = getSystemBrightnessMode(context);
            if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                color = R.color.state_enabled;
            } else {
                // Color based on brightness
                int brightness = getSystemBrightness(context);
                color = mixColors(
                        context.getResources().getColor(R.color.button_brightness_min),
                        context.getResources().getColor(R.color.button_brightness_max),
                        brightness * 100 / BRIGHTNESS_SCALE);
                methodName = "setBackgroundColor";
            }
        } catch (Exception e) {
            Log.d(TAG, "brightness: ", e);
        }
        views.setInt(getButtonId(), methodName, color);
    }

    @Override
    public void toggleState(Context context) {
        int brightness;
        int brightnessMode;

        if (brightnessLevels == null) {
            brightnessLevels = getDefinedBrightnessLevels(context);
        }
        try {
            brightnessMode = getSystemBrightnessMode(context);

            if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                brightness = toBrightness(0);
                brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
            } else {
                brightness = getSystemBrightness(context);
                int state = toBrightnessState(brightness);
                if (state < brightnessLevels.length - 1) {
                    brightness = toBrightness(state + 1);
                } else {
                    // Change to auto mode
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
                }
            }
            setSystemBrightnessMode(context, brightnessMode);
            if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                setSystemBrightness(context, brightness);
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

    private static int getSystemBrightness(Context context) throws Exception {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS);
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

    private static int[] getDefinedBrightnessLevels(Context context) {
        Resources resources = context.getResources();
        return resources.getIntArray(R.array.brightness_levels);
    }

    private static int toBrightnessState(int brightness) {
        int index;

        brightness = brightness * 100 / BRIGHTNESS_SCALE;
        for (index = 0; index < brightnessLevels.length; ++index) {
            if (brightness <= brightnessLevels[index]) {
                break;
            }
        }
        return index;
    }

    private static int toBrightness(int state) {
        return brightnessLevels[state] * BRIGHTNESS_SCALE / 100;
    }

    private static int mixColors(int col1, int col2, int percentage) {
        int r1, g1, b1, r2, g2, b2, r, g, b;

        r1 = Color.red(col1);
        g1 = Color.green(col1);
        b1 = Color.blue(col1);
        r2 = Color.red(col2);
        g2 = Color.green(col2);
        b2 = Color.blue(col2);

        r = r1 + (r2 - r1) * percentage / 100;
        g = g1 + (g2 - g1) * percentage / 100;
        b = b1 + (b2 - b1) * percentage / 100;

        return Color.rgb(r, g, b);
    }
}
