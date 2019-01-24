/**
 * AirCasting - Share your Air!
 * Copyright (C) 2011-2012 HabitatMap, Inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting;

import pl.llp.aircasting.screens.sessionRecord.ContributeActivity;
import pl.llp.aircasting.screens.sessionRecord.EditSessionActivity;
import pl.llp.aircasting.screens.sessionRecord.MakeANoteActivity;
import pl.llp.aircasting.screens.sessions.SessionsActivity;
import pl.llp.aircasting.screens.sessions.ShareSessionActivity;
import pl.llp.aircasting.screens.dashboard.DashboardActivity;
import pl.llp.aircasting.screens.stream.ThresholdsActivity;
import pl.llp.aircasting.screens.extsens.ExternalSensorActivity;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.sensor.ioio.IOIOService;
import pl.llp.aircasting.sensor.common.SensorService;
import pl.llp.aircasting.storage.DatabaseWriterService;
import pl.llp.aircasting.sessionSync.StreamingSessionsSyncService;
import pl.llp.aircasting.sessionSync.SyncService;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class Intents {
    public static final String SESSION = "session";
    public static final int SAVE_DIALOG = 0;
    public static final int EDIT_SESSION = 1;
    public static final int TAKE_PICTURE = 2;

    public static final String SESSION_SERVICE_TASK = "session_service_task";
    public static final int START_SENSORS = 0;
    public static final int STOP_SENSORS = 1;
    public static final int RESTART_SENSORS = 2;
    public static final int DISCONNECT_SENSORS = 3;
    public static final int UNKNOWN = -1;

    public static final String SESSION_ID = "session_id";
    public static final String PICTURES = "pictures";
    public static final String AIR_CASTING = "AirCasting";

    public static final String ACTION_SYNC_UPDATE = "AIRCASTING_SYNC_UPDATE";

    public static final String MESSAGE = "message";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String MIME_TEXT_CSV = "text/csv";
    public static final String MIME_APPLICATION_ZIP = "application/zip";
    public static final String MIME_IMAGE_JPEG = "image/jpeg";
    public static final int REQUEST_ENABLE_BLUETOOTH = 5;
    public static final String EXTRA_SENSOR = "sensor";
    public static final String CONFIGURATION_REQUIRED = "configuration_required";
    public static final String CONTINUE_STREAMING = "continue_streaming";
    public static final String UUID_KEY = "uuid";

    public static void editSession(Activity activity, Session session) {
        Intent intent = new Intent(activity, EditSessionActivity.class);
        intent.putExtra(SESSION, session);
        activity.startActivityForResult(intent, Intents.EDIT_SESSION);
    }

    public static Session editSessionResult(Intent data) {
        return (Session) data.getSerializableExtra(SESSION);
    }

    public static void makeANote(Activity activity) {
        Intent intent = new Intent(activity, MakeANoteActivity.class);
        activity.startActivity(intent);
    }

    public static void startSensors(Context context) {
        Intent intent = new Intent(context, SensorService.class);
        intent.putExtra(SESSION_SERVICE_TASK, START_SENSORS);

        ContextCompat.startForegroundService(context, intent);
    }

    public static void stopSensors(Context context) {
        Intent intent = new Intent(context, SensorService.class);
        intent.putExtra(SESSION_SERVICE_TASK, STOP_SENSORS);

        context.startService(intent);
    }

    public static void disconnectAllSensors(Context context) {
        Intent intent = new Intent(context, SensorService.class);
        intent.putExtra(SESSION_SERVICE_TASK, DISCONNECT_SENSORS);

        context.startService(intent);
    }

    public static void stopIOIO(Context context) {
        Intent ioioService = new Intent(context, IOIOService.class);
        context.stopService(ioioService);
    }

    public static void restartSensors(Context context) {
        Intent intent = new Intent(context, SensorService.class);
        intent.putExtra(SESSION_SERVICE_TASK, RESTART_SENSORS);

        ContextCompat.startForegroundService(context, intent);
    }

    public static void startIOIO(Context context) {
        Intent ioioService = new Intent(context, IOIOService.class);
        context.startService(ioioService);
    }

    public static int getSensorServiceTask(Intent intent) {
        if (intent != null && intent.hasExtra(SESSION_SERVICE_TASK)) {
            return intent.getIntExtra(SESSION_SERVICE_TASK, START_SENSORS);
        } else {
            return UNKNOWN;
        }
    }

    public static void share(Context context, String chooserTitle, String subject, String text) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType(MIME_TEXT_PLAIN);

        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        Intent chooser = Intent.createChooser(intent, chooserTitle);
        context.startActivity(chooser);
    }

    public static void shareCSV(Activity context, Uri uri, String chooserTitle, String subject, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(MIME_APPLICATION_ZIP);

        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        Intent chooser = Intent.createChooser(intent, chooserTitle);
        context.startActivity(chooser);
    }

    public static void triggerSync(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        context.startService(intent);
    }

    public static void triggerStreamingSessionsSync(Context context) {
        Intent intent = new Intent(context, StreamingSessionsSyncService.class);
        context.startService(intent);
    }

    public static void shareSession(Activity context, long sessionId) {
        Intent intent = new Intent(context, ShareSessionActivity.class);
        intent.putExtra(SESSION_ID, sessionId);

        context.startActivity(intent);
    }

    public static void startAirbeam2Configuration(Activity context) {
        Intent intent = new Intent(context, ExternalSensorActivity.class);
        intent.putExtra(CONFIGURATION_REQUIRED, true);

        context.startActivity(intent);
    }

    public static void continueSessionStreaming(Activity context, UUID uuid) {
        Intent intent = new Intent(context, ExternalSensorActivity.class);
        intent.putExtra(CONTINUE_STREAMING, true);
        intent.putExtra(UUID_KEY, uuid.toString());

        context.startActivity(intent);
    }

    /**
     * @param activity Context for this intent
     * @return Path where the photo will end up if it's successfully taken
     * @throws java.io.IOException When it's impossible to create the AirCasting directory
     */
    public static String takePhoto(Activity activity) throws IOException {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File target = getPhotoTarget(activity);
        Uri uri = FileProvider.getUriForFile(activity, "pl.llp.aircasting.fileprovider", target);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        activity.startActivityForResult(intent, TAKE_PICTURE);

        return target.toString();
    }

    private static File getPhotoTarget(Activity activity) throws IOException {
        File picturesDir = new File(activity.getFilesDir(), PICTURES);

        if (!picturesDir.exists()) {
            boolean success = picturesDir.mkdirs();
            if (!success) {
                throw new IOException("Could not create AirCasting dir");
            }
        }

        return new File(picturesDir, System.currentTimeMillis() + ".jpg");
    }

    public static void viewPhoto(Activity activity, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (uri.getScheme().equals("http")) {
            intent.setData(uri);
        } else {
            intent.setDataAndType(uri, MIME_IMAGE_JPEG);
        }

        activity.startActivity(intent);
    }

    public static void notifySyncUpdate(Context context) {
        notifySyncUpdate(context, null);
    }

    public static void notifySyncUpdate(Context context, String message) {
        Intent intent = new Intent(ACTION_SYNC_UPDATE);
        if (message != null) {
            intent.putExtra(MESSAGE, message);
        }

        context.sendBroadcast(intent);
    }

    public static String getSyncMessage(Intent intent) {
        if (intent.hasExtra(MESSAGE)) {
            return intent.getStringExtra(MESSAGE);
        } else {
            return null;
        }
    }

    public static void requestEnableBluetooth(Activity context) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
    }

    public static void startDashboardActivity(Activity activity) {
        Intent intent = new Intent(activity, DashboardActivity.class);
        activity.startActivity(intent);
    }

    public static void thresholdsEditor(Activity activity, Sensor sensor) {
        Intent intent = new Intent(activity, ThresholdsActivity.class);
        intent.putExtra(EXTRA_SENSOR, sensor);
        activity.startActivity(intent);
    }

    public static void sessions(Activity activity, Context context) {
        activity.startActivity(new Intent(context, SessionsActivity.class));
    }

    public static void startDatabaseWriterService(Context context) {
        Intent intent = new Intent(context, DatabaseWriterService.class);
        context.startService(intent);
    }

    public static void contribute(Activity activity, long sessionId) {
        Intent intent = new Intent(activity, ContributeActivity.class);
        intent.putExtra(SESSION_ID, sessionId);
        activity.startActivity(intent);
    }
}

