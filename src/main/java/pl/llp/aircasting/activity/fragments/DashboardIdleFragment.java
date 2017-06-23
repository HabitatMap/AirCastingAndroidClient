package pl.llp.aircasting.activity.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.extsens.ExternalSensorActivity;

/**
 * Created by radek on 23/06/17.
 */
public class DashboardIdleFragment extends Fragment implements View.OnClickListener {
    private View view;
    private Button microphoneButton;
    private Button sensorsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dashboard_idle_fragment, container, false);

        microphoneButton = (Button) view.findViewById(R.id.dashboard_microphone);
        sensorsButton = (Button) view.findViewById(R.id.dashboard_sensors);

        microphoneButton.setOnClickListener(this);
        sensorsButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.dashboard_microphone:
                DashboardSessionFragment idleSessionFragment = new DashboardSessionFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, idleSessionFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.dashboard_sensors:
                startActivity(new Intent(getActivity(), ExternalSensorActivity.class));
                break;
        }
    }
}
