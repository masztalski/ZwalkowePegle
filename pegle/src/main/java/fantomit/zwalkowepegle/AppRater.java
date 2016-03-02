package fantomit.zwalkowepegle;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import fantomit.zwalkowepegle.dialogs.AppRaterDialog;

public class AppRater {
    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    public static void app_launched(Context mContext, FragmentManager fm) {
        if(mContext == null){
            return;
        }
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if(prefs == null) {
            Toast.makeText(mContext, "Wyst¹pi³ b³¹d w AppRater - powiadom developera", Toast.LENGTH_SHORT).show();
            return;
        }
        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                AppRaterDialog dialog = new AppRaterDialog();
                dialog.setEditor(editor);
                dialog.show(fm, "AppRater Dialog");
            }
        }
        editor.commit();
    }
}
