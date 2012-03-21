package pl.llp.aircasting.activity.adapter;

import android.app.Activity;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Map;

@Singleton
public class StreamAdapterFactory {
    @Inject EventBus eventBus;

    public StreamAdapter getAdapter(Activity context) {
        return new StreamAdapter(context, new ArrayList<Map<String, String>>(), eventBus);
    }
}
