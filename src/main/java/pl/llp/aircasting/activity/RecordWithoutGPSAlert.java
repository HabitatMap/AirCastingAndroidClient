package pl.llp.aircasting.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatDelegate;
import pl.llp.aircasting.model.SessionManager;

public class RecordWithoutGPSAlert {
    private Activity activity;
    private AppCompatDelegate delegate;
    private SessionManager sessionManager;
    private String sessionTitle;
    private String sessionTags;
    private String sessionDescription;
    private boolean withoutLocation;

    public RecordWithoutGPSAlert(String title,
                                 String tags,
                                 String description,
                                 Activity activity,
                                 AppCompatDelegate delegate,
                                 SessionManager sessionManager,
                                 boolean withoutLocation) {
        this.activity = activity;
        this.delegate = delegate;
        this.sessionManager = sessionManager;
        this.sessionTitle = title;
        this.sessionTags = tags;
        this.sessionDescription = description;
        this.withoutLocation = withoutLocation;
    }

    public void display() {
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        sessionManager.startMobileSession(sessionTitle, sessionTags, sessionDescription, withoutLocation);
                        delegate.invalidateOptionsMenu();
                        activity.finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Without location data you can't map your session or contribute it to the CrowdMap")
                .setPositiveButton("Continue", dialogOnClickListener)
                .setNegativeButton("Cancel", dialogOnClickListener).show();
    }
}
