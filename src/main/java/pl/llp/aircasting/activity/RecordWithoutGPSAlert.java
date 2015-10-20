package pl.llp.aircasting.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import pl.llp.aircasting.model.SessionManager;

public class RecordWithoutGPSAlert {
    private Context context;
    private SessionManager sessionManager;
    private ButtonsActivity buttonsActivity;
    private boolean withoutLocation;

    public RecordWithoutGPSAlert(Context context, SessionManager sessionManager, ButtonsActivity buttonsActivity, boolean withoutLocation) {
        this.context = context;
        this.sessionManager = sessionManager;
        this.buttonsActivity = buttonsActivity;
        this.withoutLocation = withoutLocation;
    }

    public void display() {
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        sessionManager.startSession(withoutLocation);
                        buttonsActivity.update();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Without a GPS fix you can't map your session or contribute it to the CrowdMap")
                .setPositiveButton("Continue", dialogOnClickListener)
                .setNegativeButton("Cancel", dialogOnClickListener).show();
    }
}
