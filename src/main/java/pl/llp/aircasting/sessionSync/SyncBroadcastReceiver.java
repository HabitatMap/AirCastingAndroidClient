package pl.llp.aircasting.sessionSync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 2/3/12
 * Time: 11:46 AM
 */
public class SyncBroadcastReceiver extends BroadcastReceiver {
    public static final IntentFilter INTENT_FILTER = new IntentFilter(Intents.ACTION_SYNC_UPDATE);

    @Inject Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = Intents.getSyncMessage(intent);
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
