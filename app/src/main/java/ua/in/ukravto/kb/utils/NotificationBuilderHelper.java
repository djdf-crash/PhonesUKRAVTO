package ua.in.ukravto.kb.utils;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import ua.in.ukravto.kb.R;

public class NotificationBuilderHelper {

    private static final String CHANNEL_ID = "kb.ukravto.in.ua";

    public static NotificationCompat.Builder buildMessage(Context ctx, String title, String text, int priority, String category) {

        createNotificationChannel(ctx);

        return new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(priority)
                .setCategory(category)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);
    }

    private static void createNotificationChannel(Context ctx) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ctx.getString(R.string.channel_name);
            String description = ctx.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
