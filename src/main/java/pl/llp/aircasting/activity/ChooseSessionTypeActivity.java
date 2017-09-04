package pl.llp.aircasting.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.view.View;
import android.widget.Button;
import pl.llp.aircasting.R;
import roboguice.inject.InjectView;

/**
 * Created by radek on 01/09/17.
 */
public class ChooseSessionTypeActivity extends DialogActivity implements View.OnClickListener, AppCompatCallback {
    @InjectView(R.id.mobile_session_button) Button mobileSessionButton;
    @InjectView(R.id.fixed_session_button) Button fixedSessionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_type);

        mobileSessionButton.setOnClickListener(this);
        fixedSessionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        setResult(view.getId());
        finish();
    }
}
