package pl.llp.aircasting.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.adapter.RegressionAdapterFactory;
import roboguice.inject.InjectView;

/**
 * Created by marcin on 25/07/14.
 */
public class RegressionsActivity extends DialogActivity {
    @InjectView(R.id.regressions_list_view) ListView regressionsList;
    @Inject RegressionAdapterFactory regressionAdapterFactory;
    private ListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regressions_list);
        adapter = regressionAdapterFactory.create(this);
        regressionsList.setAdapter(adapter);
    }
}
