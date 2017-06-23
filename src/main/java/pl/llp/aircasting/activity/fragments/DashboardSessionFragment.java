package pl.llp.aircasting.activity.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import pl.llp.aircasting.R;

/**
 * Created by radek on 23/06/17.
 */
public class DashboardSessionFragment extends Fragment {
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dashboard_session_fragment, container, false);
        return view;
    }
}
