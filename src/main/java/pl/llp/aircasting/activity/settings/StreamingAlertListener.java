package pl.llp.aircasting.activity.settings;

import android.os.AsyncTask;
import android.preference.Preference;
import pl.llp.aircasting.util.http.HttpResult;

import static pl.llp.aircasting.util.http.HttpBuilder.http;

/**
 * Created by radek on 08/07/18.
 */
public class StreamingAlertListener implements Preference.OnPreferenceChangeListener {
    private static final String STREAMING_ALERT_PATH = "/api/user/toggle_streaming_alert.json";
    private static final String STREAMING_ALERT = "streaming_alert";

    @Override
    public boolean onPreferenceChange(Preference preference, final Object newValue) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                sendStreamingPreference((Boolean) newValue);
                return null;
            }
        }.execute();

        return true;
    }

    private HttpResult<Void> sendStreamingPreference(boolean value) {
        return http()
                .post()
                .to(STREAMING_ALERT_PATH)
                .with(STREAMING_ALERT, String.valueOf(value))
                .execute();
    }
}
