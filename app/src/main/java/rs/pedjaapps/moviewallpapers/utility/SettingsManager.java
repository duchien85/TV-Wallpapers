package rs.pedjaapps.moviewallpapers.utility;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import rs.pedjaapps.moviewallpapers.App;
import rs.pedjaapps.moviewallpapers.BuildConfig;


/**
 * Created by pedja on 2/12/14.
 * Handles all reads and writes to SharedPreferences
 *
 * @author Predrag Čokulov
 */
public class SettingsManager
{
    private static final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());

    public enum KEY
    {
        DEBUG, FIRST_LAUNCH
    }

    public static boolean isFirstLaunch()
    {
        return prefs.getBoolean(KEY.FIRST_LAUNCH.toString(), true);
    }

    public static void setFirstLaunch(boolean firstLaunch)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY.FIRST_LAUNCH.toString(), firstLaunch);
        editor.apply();
    }

    public static boolean DEBUG()
    {
        return prefs.getBoolean(KEY.DEBUG.toString(), BuildConfig.DEBUG);
    }

    public static void DEBUG(boolean debug)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY.DEBUG.toString(), debug);
        editor.apply();
    }

    public static void clearAllPrefs()
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}
