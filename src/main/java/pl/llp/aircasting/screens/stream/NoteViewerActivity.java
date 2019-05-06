package pl.llp.aircasting.screens.stream;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

import org.w3c.dom.Text;

import java.util.Date;

import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.screens.common.base.DialogActivity;
import pl.llp.aircasting.screens.common.helpers.FormatHelper;
import pl.llp.aircasting.screens.common.helpers.PhotoHelper;
import pl.llp.aircasting.screens.common.sessionState.VisibleSession;

import static pl.llp.aircasting.screens.stream.base.AirCastingActivity.NOTE_INDEX;

public class NoteViewerActivity extends DialogActivity implements View.OnClickListener {
    @Inject VisibleSession mVisibleSession;
    @Inject PhotoHelper mPhotoHelper;

    private View mPreviousNote;
    private View mNextNote;
    private TextView mNoteDate;
    private TextView mNoteNumber;
    private TextView mNoteText;
    private Button mNoteSave;
    private Button mNoteDelete;
    private Button mViewPhoto;

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

        mNotesTotal = mVisibleSession.getSessionNoteCount();
        mNoteIndex = getIntent().getIntExtra(NOTE_INDEX, 0);

        setNote();
    }

    private void setNote() {
        int index = ((mNoteIndex % mNotesTotal) + mNotesTotal) % mNotesTotal;
        Note note = mVisibleSession.getSessionNote(index);
        String title = FormatHelper.dateTime(note.getDate()).toString();
        mNoteDate.setText(title);

        mNoteNumber.setText(index + 1 + "/" + mNotesTotal);
        mNoteText.setText(note.getText());
        mViewPhoto.setVisibility(mPhotoHelper.photoExists(note) ? View.VISIBLE : View.GONE);

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
        }
    }
}
