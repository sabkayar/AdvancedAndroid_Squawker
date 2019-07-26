package android.example.com.squawker.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import timber.log.Timber;

import static android.content.ContentValues.TAG;


public class SquawkerMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "squawker_notification_channel";
    private static final int NOTIFICATION_ID = 100;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }


    // For Receiving data messages
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String author = data.get(SquawkContract.COLUMN_AUTHOR);
        String authorKey = data.get(SquawkContract.COLUMN_AUTHOR_KEY);
        String message = data.get(SquawkContract.COLUMN_MESSAGE);
        String date = data.get(SquawkContract.COLUMN_DATE);
        Timber.i(author + ":" + authorKey + ":" + message + ":" + date);

        ContentValues contentValues = new ContentValues();
        contentValues.put(SquawkContract.COLUMN_AUTHOR, author);
        contentValues.put(SquawkContract.COLUMN_AUTHOR_KEY, authorKey);
        contentValues.put(SquawkContract.COLUMN_MESSAGE, message);
        contentValues.put(SquawkContract.COLUMN_DATE, date);
        getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, contentValues);


        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //Create Notification channel and return channel id if sdk >= Android Oreo else return null
        String channelId = createNotificationChannel();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_small_notification)
                .setContentTitle(author)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
// notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());


    }

    private String createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
            return CHANNEL_ID;


        } else {
            return null;
        }
    }


    private void sendRegistrationToServer(String token) {
    }
}
