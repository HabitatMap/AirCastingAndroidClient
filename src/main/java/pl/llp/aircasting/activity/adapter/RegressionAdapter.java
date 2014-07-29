package pl.llp.aircasting.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Regression;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.storage.repository.RegressionRepository;

import java.util.List;

/**
 * Created by marcin on 28/07/14.
 */
public class RegressionAdapter extends ArrayAdapter {

    private List<Regression> regressions;
    private RegressionRepository regressionRepository;
    private SessionManager sessionManager;

    public RegressionAdapter(Context context, List<Regression> regressions,
                             RegressionRepository regressionRepository, SessionManager sessionManager) {
        super(context, R.layout.regression_row, R.id.target_name, regressions);
        this.regressions = regressions;
        this.regressionRepository = regressionRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Regression regression = regressions.get(position);
        ((TextView) view.findViewById(R.id.target_name)).setText(regression.getSensorName() + " " + regression.getSensorPackageName());
        ((TextView) view.findViewById(R.id.reference_name)).setText(regression.getReferenceSensorName() + " " + regression.getReferenceSensorPackageName());
        final CheckBox enabled = (CheckBox) view.findViewById(R.id.regression_enabled);
        enabled.setChecked(regression.isEnabled());
        enabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regressionRepository.setEnabled(regressions.get(position), enabled.isChecked());
                sessionManager.refreshUnits();
            }
        });
        return view;
    }

}
