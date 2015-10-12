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

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        int id = Integer.parseInt(extras.getString(_ID));
        String name = extras.getString(_NAME);

        PendingIntent contentIntent = PendingIntent.getActivity(context, id, new Intent(context, MainActivity.class), 0);

        long[] pattern = {2,1000,500,2000,500,3000};
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_water_white_36dp)
                .setContentTitle(name)
                .setContentText("Woda!!")
                .setContentIntent(contentIntent)
                .setVibrate(pattern)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }
}
