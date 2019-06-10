package pl.llp.aircasting.screens.dashboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.base.DialogActivity;
import pl.llp.aircasting.screens.stream.graph.GraphActivity;
import pl.llp.aircasting.screens.stream.map.AirCastingMapActivity;
import pl.llp.aircasting.screens.stream.map.StreamMapActivity;
import roboguice.inject.InjectView;

/**
 * Created by radek on 04/08/17.
 */
public class StreamOptionsActivity extends DialogActivity implements View.OnClickListener {
    @Inject Context context;
    @InjectView(R.id.graph_button) Button graphButton;
    @InjectView(R.id.map_button) Button mapButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_stream);
        initDialogToolbar("Stream View");

        graphButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);
   }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.graph_button:
                startActivity(new Intent(context, GraphActivity.class));
                finish();
                break;
            case R.id.map_button:
                startActivity(new Intent(context, StreamMapActivity.class));
                finish();
                break;
        }
    }
}
