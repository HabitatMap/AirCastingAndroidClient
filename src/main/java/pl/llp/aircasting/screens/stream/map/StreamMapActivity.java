package pl.llp.aircasting.screens.stream.map;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.stream.base.AirCastingActivity;

public class StreamMapActivity extends AirCastingActivity {
    private StreamMapViewMvcImpl mStreamMapViewMvcImpl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStreamMapViewMvcImpl = new StreamMapViewMvcImpl(this, null);
        setContentView(mStreamMapViewMvcImpl.getRootView());
        initToolbar("Map");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuInflater inflater = getDelegate().getMenuInflater();
        inflater.inflate(R.menu.toolbar_crowd_map_toggle, menu);
        return true;
    }

    @Override
    protected void refreshNotes() {

    }

    @Override
    public void onLocationSettingsSatisfied() {

    }
}
