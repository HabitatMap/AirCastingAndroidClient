package pl.llp.aircasting.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by radek on 15/11/17.
 */
public class FakeActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finish();
    }
}
