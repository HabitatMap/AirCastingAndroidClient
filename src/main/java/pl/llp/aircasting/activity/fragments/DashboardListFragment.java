package pl.llp.aircasting.activity.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.DashboardActivity;
import pl.llp.aircasting.activity.DashboardBaseActivity;
import pl.llp.aircasting.activity.adapter.StreamAdapter;
import pl.llp.aircasting.activity.adapter.StreamAdapterFactory;
import pl.llp.aircasting.activity.extsens.ExternalSensorActivity;
import pl.llp.aircasting.view.DashboardListView;

/**
 * Created by radek on 28/06/17.
 */
public class DashboardListFragment extends ListFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private View view;
    private Button microphoneButton;
    private Button sensorsButton;
    private StreamAdapterFactory adapterFactory;
    private StreamAdapter adapter;
    private ApplicationState state;

    public DashboardListFragment() {
    }

    public static DashboardListFragment newInstance(StreamAdapterFactory adapterFactory,
                                                    ApplicationState state) {
        DashboardListFragment fragment = new DashboardListFragment();
        fragment.setData(adapterFactory, state);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.dashboard_list_fragment, container, false);

        microphoneButton = (Button) view.findViewById(R.id.dashboard_microphone);
        sensorsButton = (Button) view.findViewById(R.id.dashboard_sensors);

        setListAdapter(adapter);

        if (microphoneButton != null) { microphoneButton.setOnClickListener(this); }
        if (sensorsButton != null) { sensorsButton.setOnClickListener(this); }

        return view;
    }

    @Override
    public void onResume() {
        if (state.dashboardState().isPopulated()) {
            setListAdapter(adapter);
            adapter.forceUpdate();
        }

        DashboardListView listView = (DashboardListView) getListView();
        listView.setOnItemClickListener(this);

        adapter = adapterFactory.getAdapter((DashboardBaseActivity) getActivity());

        adapter.resetAllStaticCharts();
        adapter.resetDynamicCharts();
        adapter.start();
        adapter.notifyDataSetChanged();

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.dashboard_microphone:
                DashboardActivity activity = (DashboardActivity) getActivity();
                activity.connectPhoneMicrophone();
                setListAdapter(adapter);
                state.dashboardState.populate();
                activity.invalidateOptionsMenu();
                break;
            case R.id.dashboard_sensors:
                startActivity(new Intent(getActivity(), ExternalSensorActivity.class));
                break;
        }
    }

    private void setData(StreamAdapterFactory adapterFactory, ApplicationState state) {
        this.adapterFactory = adapterFactory;
        this.state = state;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DashboardActivity activity = (DashboardActivity) getActivity();
        View item = getListAdapter().getView(position, view, parent);

        activity.viewChartOptions(item);
    }
}

