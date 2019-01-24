package pl.llp.aircasting.screens.common.helpers;

import android.support.v4.app.NotificationCompat;
import pl.llp.aircasting.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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

    @Inject NotificationManager notificationManager;
    @Inject Context context;

    @InjectResource(R.string.aircasting_is_recording) String aircastingIsRecording;

    public void showRecordingNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(aircastingIsRecording)
                .setContentText("")
                .setSmallIcon(R.drawable.ic_media_record)
                .setAutoCancel(true);

        builder.build();

        Notification notification = builder.getNotification();
        notificationManager.notify(RECORDING_ID, notification);
    }

    public void hideRecordingNotification() {
        notificationManager.cancel(RECORDING_ID);
    }
}
