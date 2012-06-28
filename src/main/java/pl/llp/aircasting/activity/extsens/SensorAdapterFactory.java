package pl.llp.aircasting.activity.extsens;

import android.app.Activity;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;

import java.util.Map;

@Singleton
public class SensorAdapterFactory {
    public SensorAdapter getAdapter(Activity context) {
        return new SensorAdapter(context, Lists.<Map<String, String>>newArrayList());
    }
}
