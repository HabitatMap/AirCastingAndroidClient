/**
 AirCasting - Share your Air!
 Copyright (C) 2011-2012 HabitatMap, Inc.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;

public class SplashActivity extends Activity {

    public static final int MIN_SPLASH_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //noinspection unchecked
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // This may take some time
                long startTime = System.currentTimeMillis();

                long sleepTime = sleepTime(startTime);
                sleep(sleepTime);

                return null;
            }

            private long sleepTime(long startTime) {
                long elapsed = System.currentTimeMillis() - startTime;

                return Math.max(MIN_SPLASH_TIME - elapsed, 0);
            }

            private void sleep(long sleepTime) {
                try {
                    // Make sure the splash appears for at least a moment
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                startActivity(new Intent(getApplication(), DashboardActivity.class));
            }
        }.execute();
    }
}
