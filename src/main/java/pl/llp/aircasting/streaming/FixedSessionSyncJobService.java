package pl.llp.aircasting.streaming;

import android.util.Log;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by radek on 16/01/18.
 */
public class FixedSessionSyncJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.i("session sync job", "started");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.i("session sync job", "stopped");
        return false;
    }
}