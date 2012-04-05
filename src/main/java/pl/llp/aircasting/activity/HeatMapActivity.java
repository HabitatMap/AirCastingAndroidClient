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

import pl.llp.aircasting.R;
import pl.llp.aircasting.api.AveragesDriver;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.LocationConversionHelper;
import pl.llp.aircasting.model.Region;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.view.MapIdleDetector;
import pl.llp.aircasting.view.overlay.HeatMapOverlay;

import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import com.google.inject.Inject;

import static java.lang.Math.min;
import static pl.llp.aircasting.view.MapIdleDetector.detectMapIdle;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/17/11
 * Time: 5:04 PM
 */
public class HeatMapActivity extends AirCastingMapActivity implements MapIdleDetector.MapIdleListener {
    public static final int HEAT_MAP_UPDATE_TIMEOUT = 500;
    public static final int SOUND_TRACE_UPDATE_TIMEOUT = 300;

    @Inject HeatMapOverlay heatMapOverlay;
    @Inject AveragesDriver averagesDriver;
    @Inject ConnectivityManager connectivityManager;

    private boolean soundTraceComplete = true;
    private int requestsOutstanding = 0;
    private AsyncTask<Void, Void, Void> refreshTask;
    private MapIdleDetector heatMapDetector;
    private MapIdleDetector soundTraceDetector;
    private HeatMapUpdater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heat_map);

        mapView.getOverlays().add(heatMapOverlay);
        mapView.getOverlays().add(routeOverlay);
        mapView.getOverlays().add(traceOverlay);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkConnection();

        updater = new HeatMapUpdater();
        heatMapDetector = detectMapIdle(mapView, HEAT_MAP_UPDATE_TIMEOUT, updater);
        soundTraceDetector = detectMapIdle(mapView, SOUND_TRACE_UPDATE_TIMEOUT, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        heatMapDetector.stop();
        soundTraceDetector.stop();
    }

    @Override
    public void onEvent(ViewStreamEvent event) {
        super.onEvent(event);

        updater.onMapIdle();
        onMapIdle();
    }

    private void checkConnection() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void refresh() {
        boolean complete = (requestsOutstanding == 0) && soundTraceComplete;
        if (complete) {
            stopSpinner();
        } else {
            startSpinner();
        }
        if (!complete) mapView.invalidate();
    }

    @Override
    public void onMapIdle() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshSoundTrace();
            }
        });
    }

    private void refreshSoundTrace() {
        if (refreshTask != null && refreshTask.getStatus() != AsyncTask.Status.FINISHED) return;

        //noinspection unchecked
        refreshTask = new UpdateSoundTraceTask().execute();
    }

    class HeatMapDownloader extends AsyncTask<Void, Void, HttpResult<Iterable<Region>>> {
        public static final int MAP_BUFFER_SIZE = 3;

        @Override
        protected void onPreExecute() {
            requestsOutstanding += 1;
            refresh();
        }

        @Override
        protected HttpResult<Iterable<Region>> doInBackground(Void... voids) {
            Projection projection = mapView.getProjection();

            // We want to download data that's off screen so the user can see something while panning
            GeoPoint northWest = projection.fromPixels(-mapView.getWidth(), -mapView.getHeight());
            GeoPoint southEast = projection.fromPixels(2 * mapView.getWidth(), 2 * mapView.getHeight());

            Location northWestLoc = LocationConversionHelper.location(northWest);
            Location southEastLoc = LocationConversionHelper.location(southEast);

            int size = min(mapView.getWidth(), mapView.getHeight()) / settingsHelper.getHeatMapDensity();
            if (size < 1) size = 1;

            int gridSizeX = MAP_BUFFER_SIZE * mapView.getWidth() / size;
            int gridSizeY = MAP_BUFFER_SIZE * mapView.getHeight() / size;

            return averagesDriver.index(northWestLoc.getLongitude(), northWestLoc.getLatitude(),
                    southEastLoc.getLongitude(), southEastLoc.getLatitude(), gridSizeX, gridSizeY);
        }

        @Override
        protected void onPostExecute(HttpResult<Iterable<Region>> regions) {
            requestsOutstanding -= 1;

            if (regions.getContent() != null) {
                heatMapOverlay.setRegions(regions.getContent());
            }

            mapView.invalidate();
            refresh();
        }
    }

    class UpdateSoundTraceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            soundTraceComplete = false;
            refresh();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            traceOverlay.refresh(mapView);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            soundTraceComplete = true;
            mapView.invalidate();
            refresh();
        }
    }

    private class HeatMapUpdater implements MapIdleDetector.MapIdleListener {
        @Override
        public void onMapIdle() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //noinspection unchecked
                    new HeatMapDownloader().execute();
                }
            });
        }
    }
}