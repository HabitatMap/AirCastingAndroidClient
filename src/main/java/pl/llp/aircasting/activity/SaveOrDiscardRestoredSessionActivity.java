package pl.llp.aircasting.activity;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

public class SaveOrDiscardRestoredSessionActivity extends DialogActivity implements View.OnClickListener
{
  @InjectView(R.id.save_button) Button saveButton;
  @InjectView(R.id.discard_button) Button discardButton;

  @InjectView(R.id.session_title) EditText sessionTitle;
  @InjectView(R.id.session_tags) EditText sessionTags;
  @InjectView(R.id.session_description) EditText sessionDescription;

  @Inject SessionManager sessionManager;
  @Inject SettingsHelper settingsHelper;
  @Inject MetadataHelper metadataHelper;

  @Inject ApplicationState state;

  private long sessionId;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    sessionManager.pauseSession();

    setContentView(R.layout.save_lost_session);

    saveButton.setOnClickListener(this);
    discardButton.setOnClickListener(this);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    if(!getIntent().hasExtra(Intents.SESSION_ID))
    {
      throw new RuntimeException("Should have arrived here with a session id");
    }

    sessionId = getIntent().getLongExtra(Intents.SESSION_ID, 0);
    state.saving().markCurrentlySaving(sessionId);
  }

  @Override
  public void onBackPressed()
  {
    sessionManager.discardSession(sessionId);
    finish();
  }

  @Override
  public void onClick(View view)
  {
    switch (view.getId())
    {
      case R.id.save_button:
      {
        fillSessionDetails(sessionId);
        Session session = sessionManager.getSession();
        if(session.isLocationless())
        {
          sessionManager.finishSession(sessionId);
        }
        else
        {
          Intents.contribute(this, sessionId);
        }
        break;
      }
      case R.id.discard_button:
      {
        sessionManager.discardSession(sessionId);
        break;
      }
    }
    finish();
  }

  private void fillSessionDetails(long sessionId)
  {
    String title = sessionTitle.getText().toString();
    String tags = sessionTags.getText().toString();
    String description = sessionDescription.getText().toString();
    sessionManager.setTitleTagsDescription(sessionId, title, tags, description);
  }
}
