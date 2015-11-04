package fantomit.zwalkowepegle.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import fantomit.zwalkowepegle.BuildConfig;
import fantomit.zwalkowepegle.MainActivity;
import fantomit.zwalkowepegle.R;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String _KEY = "notify";
    public static final String _ID = "ID";
    public static final String _NAME = "name";
    public static final String _dVAL = "dValue";
    public static final String _iVAL = "iValue";
    public static final String _TYPE = "type";

    private boolean czyPoziom;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        int id = Integer.parseInt(extras.getString(_ID));
        String name = extras.getString(_NAME);
        String typ = extras.getString(_TYPE);
        double przeplyw = -1;
        int poziom = -1;
        if (extras.containsKey(_dVAL)) {
            przeplyw = extras.getDouble(_dVAL);
            czyPoziom = false;
        } else if (extras.containsKey(_iVAL)) {
            poziom = extras.getInt(_iVAL);
            czyPoziom = true;
        }

        if(!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent("Notification sent")
                    .putCustomAttribute(name, (czyPoziom ? Integer.toString(poziom) + "cm" : Double.toString(przeplyw) + "m3/s") + "\n" + "dolna granica: " + typ));
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, id, new Intent(context, MainActivity.class), 0);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        long[] pattern = {2, 1000, 500, 2000, 500, 3000};
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_water_white_36dp)
                .setContentTitle(name)
                .setContentText(czyPoziom ? "Jest " + Integer.toString(poziom) + " cm" : "Jest " + Double.toString(przeplyw) + " m3/s")
                .setContentIntent(contentIntent)
                .setVibrate(pattern)
                .setLights(Color.RED, 3000, 3000)
                .setSound(alarmSound)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }
}
