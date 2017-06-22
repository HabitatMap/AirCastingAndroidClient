package pl.llp.aircasting.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatDelegate;
import pl.llp.aircasting.model.SessionManager;

public class RecordWithoutGPSAlert {
    private Context context;
    private AppCompatDelegate delegate;
    private SessionManager sessionManager;
    private boolean withoutLocation;

    public RecordWithoutGPSAlert(Context context, AppCompatDelegate delegate, SessionManager sessionManager, boolean withoutLocation) {
        this.context = context;
        this.delegate = delegate;
        this.sessionManager = sessionManager;
        this.withoutLocation = withoutLocation;
    }

    public void display() {
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        sessionManager.startMobileSession(withoutLocation);
                        delegate.invalidateOptionsMenu();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Without location data you can't map your session or contribute it to the CrowdMap")
                .setPositiveButton("Continue", dialogOnClickListener)
                .setNegativeButton("Cancel", dialogOnClickListener).show();
    }
}
