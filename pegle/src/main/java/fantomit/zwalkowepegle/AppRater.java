package fantomit.zwalkowepegle;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppRater {
    private final static String APP_TITLE = "Zwa�kowe Pegle";
    private final static String APP_PNAME = "fantomit.zwalkowepegle";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
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
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();
    }

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Oce� " + APP_TITLE);

        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(mContext);
        tv.setText("Je�li lubisz " + APP_TITLE + ", prosz� po�wi�� chwil� na ich ocen�. Dzi�ki za wsparcie!");
        tv.setWidth(240);
        tv.setPadding(10, 10, 10, 15);
        ll.addView(tv);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                500,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(15, 5, 10, 5);

        Button b1 = new Button(mContext);
        b1.setBackgroundResource(R.drawable.button_effect);
        b1.setPadding(10, 10, 10, 10);
        b1.setLayoutParams(params);
        b1.setText("Oce� " + APP_TITLE);
        b1.setOnClickListener((View v) -> {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                    }
                    dialog.dismiss();
                }
        );
        ll.addView(b1);

        Button b2 = new Button(mContext);
        b2.setBackgroundResource(R.drawable.button_effect);
        b2.setPadding(10, 10, 10, 10);
        b2.setLayoutParams(params);
        b2.setText("Przypomnij p�niej");
        b2.setOnClickListener((View v) -> {
                    dialog.dismiss();
                }
        );
        ll.addView(b2);

        Button b3 = new Button(mContext);
        b3.setBackgroundResource(R.drawable.button_effect);
        b3.setPadding(10, 10, 10, 10);
        b3.setLayoutParams(params);
        b3.setText("Nie, dzi�ki");
        b3.setOnClickListener((View v) -> {
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                    }
                    dialog.dismiss();
                }
        );
        ll.addView(b3);

        dialog.setContentView(ll);
        dialog.show();
    }
}
