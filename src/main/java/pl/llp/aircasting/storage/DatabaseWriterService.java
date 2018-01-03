package pl.llp.aircasting.storage;

import pl.llp.aircasting.storage.db.AirCastingDB;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;
import pl.llp.aircasting.util.Constants;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.google.inject.Inject;
import roboguice.service.RoboService;

import java.util.concurrent.atomic.AtomicBoolean;

import static pl.llp.aircasting.sensor.BluetoothConnector.sleepFor;

/**
 * Created by ags on 03/16/13 at 19:10
 */
public class DatabaseWriterService extends RoboService {
    public static final int DELAY_MILLISECONDS = 200;

    @Inject AirCastingDB db;
    @Inject DatabaseTaskQueue taskService;

    final AtomicBoolean shouldRun = new AtomicBoolean(true);
    private Thread thread;

    @Inject
    public void init() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (shouldRun.get()) {
                    if (taskService.somethingAvailable()) {
                        unitOfAction();
                    } else {
                        sleepFor(DELAY_MILLISECONDS);
                    }
                }
            }
        });
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }

    void unitOfAction() {
        try {
            WritableDatabaseTask task = taskService.getFirst();
            db.executeWritableTask(task);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Something bad happened", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}