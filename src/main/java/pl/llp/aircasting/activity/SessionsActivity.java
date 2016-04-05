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

import android.util.Log;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.adapter.SessionAdapter;
import pl.llp.aircasting.activity.adapter.SessionAdapterFactory;
import pl.llp.aircasting.activity.events.SessionLoadedEvent;
import pl.llp.aircasting.activity.menu.MainMenu;
import pl.llp.aircasting.activity.task.CalibrateSessionsTask;
import pl.llp.aircasting.activity.task.OpenSessionTask;
import pl.llp.aircasting.event.SyncStateChangedEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.SelectSensorHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.helper.TopBarHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.receiver.SyncBroadcastReceiver;
import pl.llp.aircasting.storage.db.UncalibratedMeasurementCalibrator;
import pl.llp.aircasting.storage.repository.SensorRepository;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.util.SyncState;

import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.jetbrains.annotations.Nullable;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;

public class SessionsActivity extends RoboListActivityWithProgress implements AdapterView.OnItemLongClickListener,
    AdapterView.OnItemSelectedListener
{
  private static final int ALL_ID = 0;

  @Inject SessionAdapterFactory sessionAdapterFactory;
  @Inject SelectSensorHelper selectSensorHelper;
  @Inject SessionRepository sessionRepository;
  @Inject SensorRepository sensorRepository;
  @Inject SessionManager sessionManager;
  @Inject SettingsHelper settingsHelper;
  @Inject SensorManager sensorManager;
  @Inject TopBarHelper topBarHelper;
  @Inject Application context;
  @Inject SyncState syncState;
  @Inject EventBus eventBus;
  @Inject MainMenu mainMenu;

  @Inject UncalibratedMeasurementCalibrator calibrator;

  @InjectResource(R.string.all) String all;

  @InjectView(R.id.sensor_spinner) Spinner sensorSpinner;
  @InjectView(R.id.sync_summary) Button syncSummary;
  @InjectView(R.id.top_bar) View topBar;

  @Inject SyncBroadcastReceiver syncBroadcastReceiver;

  BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      refreshList();
    }
  };

  private SensorsAdapter sensorAdapter;
  private SessionAdapter sessionAdapter;
  private Sensor selectedSensor;
  private long sessionId;
  private boolean calibrationAttempted;
  private DummySensor dummySensor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    calibrateOldRecords();
    setContentView(R.layout.sessions);

    getListView().setOnItemLongClickListener(this);

    dummySensor = new DummySensor(all);
    sensorAdapter = new SensorsAdapter(this, Lists.<SensorWrapper>newArrayList(), dummySensor);
    sensorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sensorAdapter.insert(dummySensor, ALL_ID);
    sensorSpinner.setAdapter(sensorAdapter);
  }

  @Override
  protected void onResume() {
    super.onResume();

    refreshList();
    refreshTopBar();

    IntentFilter filter = new IntentFilter();
    filter.addAction(Intents.ACTION_SYNC_UPDATE);
    registerReceiver(broadcastReceiver, filter);

    registerReceiver(syncBroadcastReceiver, SyncBroadcastReceiver.INTENT_FILTER);
    eventBus.register(this);
    topBar.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Intent intent = new Intent(SessionsActivity.this, ThresholdsActivity.class);
        intent.putExtra(Intents.EXTRA_SENSOR, selectedSensor);
        startActivity(intent);
      }
    });
    syncSummary.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Intents.triggerSync(SessionsActivity.this);
      }
    });
  }

  private void refreshTopBar() {
    if (selectedSensor == null) {
      topBar.setVisibility(View.GONE);
    } else {
      topBar.setVisibility(View.VISIBLE);
      topBarHelper.updateTopBar(selectedSensor, topBar);
    }
  }

  private void refreshItems()
  {
     List<Session> sessions = sessionRepository.notDeletedSessions(selectedSensor);

    if (sessionAdapter == null) {
      sessionAdapter = sessionAdapterFactory.getSessionAdapter(this);
      setListAdapter(sessionAdapter);
    }

    sessionAdapter.setSessions(sessions);
    sessionAdapter.setSelectedSensor(selectedSensor);
  }

  @Override
  protected void onPause() {
    super.onPause();

    unregisterReceiver(broadcastReceiver);
    unregisterReceiver(syncBroadcastReceiver);
    eventBus.unregister(this);
  }

  private void refreshList()
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        refreshBottomBar();
        refreshSensors();
        refreshItems();
      }
    });
  }

  private void refreshSensors() {
    List<Sensor> sensors = sensorRepository.getAll();
    Iterable<SensorWrapper> wrappers = Iterables.transform(sensors, new Function<Sensor, SensorWrapper>() {
      @Override
      public SensorWrapper apply(@Nullable Sensor input) {
        return new SensorWrapper(input);
      }
    });
    List<SensorWrapper> wrapperList = newArrayList(wrappers);

    sensorAdapter.update(wrapperList);

    sensorSpinner.setPromptId(R.string.select_sensor);
//    sensorSpinner.setAdapter(sensorAdapter);
    sensorSpinner.setOnItemSelectedListener(this);
  }

  @Subscribe
  public void onEvent(SyncStateChangedEvent event)
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        refreshBottomBar();
      }
    });
  }

  private void refreshBottomBar()
  {
    if (syncState.isInProgress())
    {
      syncSummary.setText(R.string.sync_in_progress);
    }
    else
    {
      syncSummary.setText(R.string.sync_possible);
    }
  }

  @Override
  protected void onListItemClick(ListView listView, View view, int position, long id) {
    Session s = sessionAdapter.getSession(position);
    viewSession(s.getId());
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
    Intent intent = new Intent(this, OpenSessionActivity.class);
    Session s = sessionAdapter.getSession(position);
    sessionId = s.getId();
    startActivityForResult(intent, 0);
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (resultCode) {
      case R.id.view:
        viewSession(sessionId);
        break;
      case R.id.delete_session:
        deleteSession(sessionId);
        break;
      case R.id.edit:
        editSession(sessionId);
        break;
      case R.id.save_button:
        updateSession(data);
        break;
      case R.id.share:
        Intents.shareSession(this, sessionId);
        break;
    }
  }

  private void updateSession(Intent data) {
    Session session = Intents.editSessionResult(data);

    sessionRepository.update(session);
    Intents.triggerSync(context);

    refreshList();
  }

  private void editSession(long id) {
    Session session = sessionRepository.loadShallow(id);
    Intents.editSession(this, session);
  }

  private void deleteSession(long id) {
    sessionRepository.markSessionForRemoval(id);
    Intents.triggerSync(context);

    refreshList();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return mainMenu.create(this, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return mainMenu.handleClick(this, item);
  }

  private void viewSession(long id) {
    if (sessionManager.isSessionStarted()) {
      Toast.makeText(context, R.string.stop_aircasting, Toast.LENGTH_LONG).show();
      return;
    }

    new OpenSessionTask(this) {
      @Override
      protected Session doInBackground(Long... longs) {
        sessionManager.loadSession(longs[0], this);

        return null;
      }

      @Override
      protected void onPostExecute(Session session) {
        super.onPostExecute(session);

        startSessionView();
      }
    }.execute(id);
  }

  private void startSessionView() {
    eventBus.post(new SessionLoadedEvent(sessionManager.getSession()));
    startActivity(new Intent(context, StreamsActivity.class));
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case SelectSensorHelper.DIALOG_ID:
        return selectSensorHelper.chooseSensor(this);
      default:
        return super.onCreateDialog(id);
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    selectedSensor = sensorAdapter.getItem(position).getSensor();

    refreshItems();
    refreshTopBar();
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
  }

  private static class SensorWrapper {
    private Sensor sensor;

    public SensorWrapper(Sensor sensor) {
      this.sensor = sensor;
    }

    public Sensor getSensor() {
      return sensor;
    }

    @Override
    public String toString() {
      return sensor.getShortType() + " - " + sensor.getSensorName();
    }

    @Override
    public boolean equals(Object o)
    {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SensorWrapper that = (SensorWrapper) o;

      if (!sensor.equals(that.sensor)) return false;

      return true;
    }

    @Override
    public int hashCode()
    {
      return sensor.hashCode();
    }
  }

  private static class DummySensor extends SensorWrapper {
    private String name;

    public DummySensor(String name) {
      super(null);
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private void calibrateOldRecords()
  {
    if(calibrationAttempted)
      return;

    calibrationAttempted = true;
    if(calibrator.sessionsToCalibrate() > 0)
    {
      CalibrateSessionsTask task = new CalibrateSessionsTask(this, calibrator);
      task.execute();
    }
  }

  static class SensorsAdapter extends ArrayAdapter<SensorWrapper>
  {
    private final ArrayList<SensorWrapper> data;
    private final SensorWrapper dummy;

    public SensorsAdapter(Context context, ArrayList<SensorWrapper> data, SensorWrapper dummy)
    {
      super(context, android.R.layout.simple_spinner_item, data);
      this.data = data;
      this.dummy = dummy;
    }

    public void update(List<SensorWrapper> newWrappers)
    {
      LinkedList<SensorWrapper> toRemove = newLinkedList();
      for (SensorWrapper wrapper : data)
      {
        if(dummy != wrapper && !newWrappers.contains(wrapper))
        {
          toRemove.add(wrapper);
        }
      }
      for (SensorWrapper wrapper : toRemove)
      {
        remove(wrapper);
      }

      for (SensorWrapper potentialWrapper : newWrappers)
      {
        if(getPosition(potentialWrapper) < 0)
        {
          add(potentialWrapper);
        }
      }
    }
  }
}
