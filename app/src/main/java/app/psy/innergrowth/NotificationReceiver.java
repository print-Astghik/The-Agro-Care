package app.psy.innergrowth;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Notification;
import android.os.Build;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "my_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Ստանում ենք բույսի անունը
        String plantName = intent.getStringExtra("plant_name");
        if (plantName == null || plantName.isEmpty()) {
            plantName = "your plant";
        }

        // Intent to open test12 activity when notification is clicked
        Intent notificationIntent = new Intent(context, test12.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }

        builder.setSmallIcon(R.drawable.notification) // Փոխիր ըստ անհրաժեշտության
                .setContentTitle("Reminder")
                .setContentText("Time to water: " + plantName)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, builder.build());
        }
    }
}
