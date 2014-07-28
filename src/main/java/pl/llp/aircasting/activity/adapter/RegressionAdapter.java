package pl.llp.aircasting.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Regression;

import java.util.List;

/**
 * Created by marcin on 28/07/14.
 */
public class RegressionAdapter extends ArrayAdapter {

    private List<Regression> regressions;

    public RegressionAdapter(Context context, List<Regression> regressions) {
        super(context, R.layout.regression_row, R.id.target_name, regressions);
        this.regressions = regressions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Regression regression = regressions.get(position);
        ((TextView) view.findViewById(R.id.target_name)).setText(regression.getSensorName() + " " + regression.getSensorPackageName());
        ((TextView) view.findViewById(R.id.reference_name)).setText(regression.getSensorName() + " " + regression.getSensorPackageName());
        return view;
    }

}
