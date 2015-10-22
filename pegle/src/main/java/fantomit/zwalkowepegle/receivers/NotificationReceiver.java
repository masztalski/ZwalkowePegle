package fantomit.zwalkowepegle.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import fantomit.zwalkowepegle.MainActivity;
import fantomit.zwalkowepegle.R;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String _KEY = "notify";
    public static final String _ID = "ID";
    public static final String _NAME = "name";
    public static final String _dVAL = "iValue";
    public static final String _iVAL = "iValue";

    private boolean czyPoziom;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        int id = Integer.parseInt(extras.getString(_ID));
        String name = extras.getString(_NAME);
        double przeplyw = -1;
        int poziom = -1;
        if(extras.containsKey(_dVAL)){
            przeplyw = extras.getDouble(_dVAL);
            czyPoziom = false;
        } else if(extras.containsKey(_iVAL)){
            poziom = extras.getInt(_iVAL);
            czyPoziom = true;
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, id, new Intent(context, MainActivity.class), 0);

        long[] pattern = {2,1000,500,2000,500,3000};
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_water_white_36dp)
                .setContentTitle(name)
                .setContentText(czyPoziom ? "Jest " + Integer.toString(poziom) + " cm" : "Jest " + Double.toString(przeplyw) + " m3/s")
                .setContentIntent(contentIntent)
                .setVibrate(pattern)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }
}
