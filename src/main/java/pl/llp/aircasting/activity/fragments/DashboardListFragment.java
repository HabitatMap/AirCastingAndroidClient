package pl.llp.aircasting.activity.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.DashboardActivity;
import pl.llp.aircasting.activity.DashboardBaseActivity;
import pl.llp.aircasting.activity.adapter.StreamAdapter;
import pl.llp.aircasting.activity.adapter.StreamAdapterFactory;
import pl.llp.aircasting.activity.extsens.ExternalSensorActivity;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.helper.ToastHelper;

/**
 * Created by radek on 28/06/17.
 */
public class DashboardListFragment extends ListFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private View view;
    private Button microphoneButton;
    private Button sensorsButton;
    private Button airbeam2ConfigButton;
    private StreamAdapterFactory adapterFactory;
    private StreamAdapter adapter;
    private SettingsHelper settingsHelper;
    private Context context;
    private DashboardActivity activity;
    private ListView listView;
    private int index = 0;
    private int top = 0;

    public DashboardListFragment() {
    }

    public static DashboardListFragment newInstance(SettingsHelper settingsHelper) {
        DashboardListFragment fragment = new DashboardListFragment();
        fragment.setData(settingsHelper);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.dashboard_list_fragment, container, false);

        microphoneButton = (Button) view.findViewById(R.id.dashboard_microphone);
        sensorsButton = (Button) view.findViewById(R.id.dashboard_sensors);
        airbeam2ConfigButton = (Button) view.findViewById(R.id.configure_airbeam2);

        context = getActivity();
        activity = (DashboardActivity) context;
        adapterFactory = activity.getAdapterFactory();
        adapter = adapterFactory.getAdapter(activity);

        setListAdapter(adapter);

        if (microphoneButton != null) { microphoneButton.setOnClickListener(this); }
        if (sensorsButton != null) { sensorsButton.setOnClickListener(this); }
        if (airbeam2ConfigButton != null) { airbeam2ConfigButton.setOnClickListener(this); }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        listView = getListView();
        adapterFactory = activity.getAdapterFactory();
        adapter = adapterFactory.getAdapter(activity);

        setListAdapter(adapter);

        adapter.forceUpdate();

        listView.setOnItemClickListener(this);

        adapter.resetAllStaticCharts();
        adapter.start();
        adapter.notifyDataSetChanged();
        adapter.setStartFakeActivity();

        listView.setSelectionFromTop(index, top);
    }

    @Override
    public void onPause() {
        super.onPause();

        index = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());

        adapter.stopFakeActivityCallback();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.dashboard_microphone:
                DashboardActivity activity = (DashboardActivity) context;
                activity.connectPhoneMicrophone();
                activity.reloadNavigationDrawer();
                setListAdapter(adapter);
                activity.invalidateOptionsMenu();
                break;
            case R.id.dashboard_sensors:
                startActivity(new Intent(getActivity(), ExternalSensorActivity.class));
                break;
            case R.id.configure_airbeam2:
                if (settingsHelper.hasCredentials()) {
                    Intents.startAirbeam2Configuration(getActivity());
                } else {
                    ToastHelper.show(context, R.string.sign_in_to_configure, Toast.LENGTH_SHORT);
                }
                break;
        }
    }

    private void setData(SettingsHelper settingsHelper) {
        this.settingsHelper = settingsHelper;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DashboardActivity activity = (DashboardActivity) getActivity();
        View item = getListAdapter().getView(position, view, parent);

        activity.viewChartOptions(item);
    }
}

