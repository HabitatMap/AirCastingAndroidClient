package pl.llp.aircasting.tracking;

import com.google.common.eventbus.EventBus;

import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.sessionSync.FixedSessionUploader;

public class FixedSessionTracker extends ActualSessionTracker {
    private final FixedSessionUploader fixedSessionUploader;

    FixedSessionTracker(EventBus eventBus, final Session session, DatabaseTaskQueue dbQueue, SettingsHelper settingsHelper, MetadataHelper metadataHelper, SessionRepository sessions, FixedSessionUploader fixedSessionUploader, boolean locationLess) {
        super(eventBus, session, dbQueue, settingsHelper, metadataHelper, sessions, locationLess);
        session.setEnd(session.getStart());
        this.fixedSessionUploader = fixedSessionUploader;
    }

    @Override
    protected boolean beforeSave(final Session session) {
        if (fixedSessionUploader.create(session))
            return true;
        else
            return false;
    }
}
