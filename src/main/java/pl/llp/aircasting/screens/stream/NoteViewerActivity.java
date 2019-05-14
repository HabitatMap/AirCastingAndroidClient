package pl.llp.aircasting.screens.stream;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

import java.io.File;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.screens.common.base.DialogActivity;
import pl.llp.aircasting.screens.common.base.SimpleProgressTask;
import pl.llp.aircasting.screens.common.helpers.FormatHelper;
import pl.llp.aircasting.screens.common.helpers.PhotoHelper;
import pl.llp.aircasting.screens.common.sessionState.VisibleSession;
import pl.llp.aircasting.storage.repository.NoteRepository;

import static pl.llp.aircasting.screens.stream.base.AirCastingActivity.NOTE_INDEX;
import static pl.llp.aircasting.Intents.triggerSync;

public class NoteViewerActivity extends DialogActivity implements View.OnClickListener {
    @Inject VisibleSession mVisibleSession;
    @Inject PhotoHelper mPhotoHelper;
    @Inject NoteRepository mNotesRepository;

    private View mPreviousNote;
    private View mNextNote;
    private TextView mNoteDate;
    private TextView mNoteNumber;
    private TextView mNoteText;
    private Button mNoteSave;
    private Button mNoteDelete;
    private Button mViewPhoto;

    private Note mCurrentNote;
    private int mNotesTotal = 0;
    private int mNoteIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.note_viewer);

        mPreviousNote = findViewById(R.id.note_left);
        mNextNote = findViewById(R.id.note_right);
        mNoteDate = findViewById(R.id.note_date);
        mNoteText = findViewById(R.id.note_text);
        mNoteNumber = findViewById(R.id.note_number);
        mViewPhoto = findViewById(R.id.view_photo);
        mNoteDelete = findViewById(R.id.note_delete);
        mNoteSave = findViewById(R.id.note_save);

        mNextNote.setOnClickListener(this);
        mPreviousNote.setOnClickListener(this);
        mViewPhoto.setOnClickListener(this);
        mNoteSave.setOnClickListener(this);
        mNoteDelete.setOnClickListener(this);

        mNotesTotal = mVisibleSession.getSessionNoteCount();
        mNoteIndex = getIntent().getIntExtra(NOTE_INDEX, 0);

        setNote();
    }

    private void setNote() {
        int index = ((mNoteIndex % mNotesTotal) + mNotesTotal) % mNotesTotal;
        mCurrentNote = mVisibleSession.getSessionNote(index);
        String title = FormatHelper.dateTime(mCurrentNote.getDate()).toString();
        mNoteDate.setText(title);

        mNoteNumber.setText(index + 1 + "/" + mNotesTotal);
        mNoteText.setText(mCurrentNote.getText());
        mViewPhoto.setVisibility(mPhotoHelper.photoExists(mCurrentNote) ? View.VISIBLE : View.GONE);

        mNoteIndex = index;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.note_left:
                mNoteIndex -= 1;
                setNote();
                break;
            case R.id.note_right:
                mNoteIndex += 1;
                setNote();
                break;
            case R.id.view_photo:
                showNotePhoto();
                break;
            case R.id.note_save:
                saveNote();
                break;
            case R.id.note_delete:
                deleteNote();
                break;
        }
    }

    private void saveNote() {
        String text = mNoteText.getText().toString();
        mCurrentNote.setText(text);
        final Context context = this;

        new SimpleProgressTask<Void, Void, Void>(this) {
            @Override
            protected Void doInBackground(Void... voids) {
                if (mVisibleSession.isVisibleSessionViewed()) {
                    mNotesRepository.updateNote(mCurrentNote, mVisibleSession.getSession().getId());
                    triggerSync(context);
                }
                return null;
            }
        }.execute();

        finish();
    }

    private void deleteNote() {
        final Context context = this;

        new SimpleProgressTask<Void, Void, Void>(this) {
            @Override
            protected Void doInBackground(Void... voids) {
                mNotesRepository.deleteNote(mVisibleSession.getSession().getId(), mCurrentNote.getNumber());
                mVisibleSession.deleteNote(mCurrentNote);
                triggerSync(context);

                return null;
            }
        }.execute();

        finish();
    }

    private void showNotePhoto() {
        Intents.viewPhoto(this, photoUri());
    }

    private Uri photoUri() {
        if (mPhotoHelper.photoExistsLocally(mCurrentNote)) {
            File file = new File(mCurrentNote.getPhotoPath());
            return FileProvider.getUriForFile(this, "pl.llp.aircasting.fileprovider", file);
        } else {
            return Uri.parse(mCurrentNote.getPhotoPath());
        }
    }
}
