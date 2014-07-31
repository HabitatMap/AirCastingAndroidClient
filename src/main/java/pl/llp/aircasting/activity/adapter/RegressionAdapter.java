package pl.llp.aircasting.activity.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import pl.llp.aircasting.R;
import pl.llp.aircasting.api.RegressionDriver;
import pl.llp.aircasting.api.data.DeleteRegressionResponse;
import pl.llp.aircasting.model.Regression;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.storage.repository.RegressionRepository;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.Status;

import java.util.List;

import static pl.llp.aircasting.Intents.triggerSync;

/**
 * Created by marcin on 28/07/14.
 */
public class RegressionAdapter extends ArrayAdapter {

    private List<Regression> regressions;
    private RegressionRepository regressionRepository;
    private RegressionDriver regressionDriver;
    private Context context;

    public RegressionAdapter(Context context, List<Regression> regressions,
                             RegressionRepository regressionRepository, RegressionDriver regressionDriver) {
        super(context, R.layout.regression_row, R.id.target_name, regressions);
        this.regressions = regressions;
        this.regressionRepository = regressionRepository;
        this.regressionDriver = regressionDriver;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Regression regression = regressions.get(position);
        ((TextView) view.findViewById(R.id.target_name)).setText(regression.getSensorName() + " " + regression.getSensorPackageName());
        ((TextView) view.findViewById(R.id.reference_name)).setText(regression.getReferenceSensorName() + " " + regression.getReferenceSensorPackageName());
        ((TextView) view.findViewById(R.id.calibration_date)).setText(regression.formattedDate());
        final CheckBox enabled = (CheckBox) view.findViewById(R.id.regression_enabled);
        enabled.setChecked(regression.isEnabled());
        enabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regressionRepository.setEnabled(regressions.get(position), enabled.isChecked());
                notifyDataSetChanged();
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Regression regression = regressions.get(position);
                if (regression.isOwner()) {
                    new AlertDialog.Builder(context)
                            .setCancelable(true)
                            .setMessage(R.string.confirm_remove_calibration)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    removeRegression(regression, position);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
                return true;
            }
        });
        return view;
    }

    private void removeRegression(Regression regression, int position) {
        HttpResult<DeleteRegressionResponse> result = regressionDriver.delete(regression);
        if (result.getStatus() == Status.SUCCESS) {
            triggerSync(context);
            regressions.remove(position);
            regressionRepository.delete(regression);
            notifyDataSetChanged();
        }
    }

}
