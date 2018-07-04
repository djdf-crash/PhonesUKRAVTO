package ua.in.ukravto.kb.utils;


import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import ua.in.ukravto.kb.R;

public class NotificationBuilderHelper {
    public static NotificationCompat.Builder buildMessage(Context ctx, PendingIntent pendingIntent, String title, String text) {
         return new NotificationCompat.Builder(ctx, "kb.ukravto.in.ua")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }
}
