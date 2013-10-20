package pl.llp.aircasting.helper;

import pl.llp.aircasting.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.activity.StreamsActivity;
import roboguice.inject.InjectResource;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 2/29/12
 * Time: 2:07 PM
 */
@Singleton
public class NotificationHelper {
    public static final int RECORDING_ID = 1;
    public static final int EMPTY_FLAGS = 0;
    public static final int REQUEST_ANY = 0;

    @Inject NotificationManager notificationManager;
    @Inject Context context;

    @InjectResource(R.string.aircasting_is_recording) String aircastingIsRecording;

    public void showRecordingNotification() {
        long time = System.currentTimeMillis();
        Notification notification = new Notification(R.drawable.ic_media_record, aircastingIsRecording, time);
        notification.flags &= ~Notification.FLAG_AUTO_CANCEL;

        Intent notificationIntent = new Intent(context, StreamsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, REQUEST_ANY, notificationIntent, EMPTY_FLAGS);
        notification.setLatestEventInfo(context, aircastingIsRecording, "", pendingIntent);

        notificationManager.notify(RECORDING_ID, notification);
    }

    public void hideRecordingNotification() {
        notificationManager.cancel(RECORDING_ID);
    }
}
