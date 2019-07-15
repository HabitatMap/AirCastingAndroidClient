/**
 AirCasting - Share your Air!
 Copyright (C) 2011-2012 HabitatMap, Inc.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.model;

import com.google.common.base.Predicate;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;
import pl.llp.aircasting.R;

import java.io.Serializable;
import java.util.*;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class Session implements Serializable {
    @Expose @SerializedName("streams") Map<String, MeasurementStream> streams = newHashMap();
    @Expose @SerializedName("tag_list") private String tags;
    @Expose @SerializedName("deleted") private boolean markedForRemoval;
    @Expose @SerializedName("start_time") private Date start = new Date();
    @Expose @SerializedName("end_time") private Date end;

    @Expose private List<Note> notes = newArrayList();
    @Expose private UUID uuid = UUID.randomUUID();

    @Expose private String title;
    @Expose private String location;
    @Expose private String type = "MobileSession";
    @Expose private int calibration;
    @Expose private boolean contribute;
    @Expose private int drawable = R.drawable.session_mobile_icon;
    @Expose private boolean isIndoor;
    @Expose private double latitude;
    @Expose private double longitude;
    @Expose private int version;

    private volatile Long id = null;
    private boolean submittedForRemoval = false;
    private boolean locationless = false;

    public Session() {}

    public Session(boolean isFixed) {
        this.setFixed(isFixed);

        if (this.isFixed()) {
            setContribute(true);
        }
    }

    public void add(MeasurementStream stream) {
        streams.put(stream.getSensorName(), stream);
    }

    public void setTitle(String text) {
        title = text;
    }

    public void setTags(String text) {
        tags = text;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasTitle() {
        return title != null && title.length() > 0;
    }

    public String getTags() {
        return tags;
    }

    public Date getEnd() {
        return end;
    }

    public Date getStart() {
        return start;
    }

    public void setEnd(Date end) {
        if (end.before(getStart())) {
            this.end = getStart();
        } else {
            this.end = end;
        }
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void add(Note note) {
        notes.add(note);
        note.setNumber(notes.size());
    }

    public void addAll(Collection<Note> notesToAdd) {
        notes.addAll(notesToAdd);
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public int getCalibration() {
        return calibration;
    }

    public void setCalibration(int calibration) {
        this.calibration = calibration;
    }

    public void setContribute(boolean contribute) {
        this.contribute = contribute;
    }

    public boolean getContribute() {
        return contribute;
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public void setMarkedForRemoval(boolean markedForRemoval) {
        this.markedForRemoval = markedForRemoval;
    }

    public void deleteNote(Note note) {
        notes.remove(note);
        reorderNotes();
    }

    private void reorderNotes() {
        for (int i = 0; i < notes.size(); i++) {
            Note note1 = notes.get(i);
            note1.setNumber(i + 1);
        }
    }

    public void setSubmittedForRemoval(boolean submittedForRemoval) {
        this.submittedForRemoval = submittedForRemoval;
    }

    public boolean isSubmittedForRemoval() {
        return submittedForRemoval;
    }

    public Collection<MeasurementStream> getMeasurementStreams() {
        return streams.values();
    }

    public MeasurementStream getStream(String sensorName) {
        return streams.get(sensorName);
    }

    public boolean hasStream(String sensorName) {
        return streams.containsKey(sensorName);
    }

    public boolean isEmpty() { return all(streams.values(), new Predicate<MeasurementStream>() {
            @Override
            public boolean apply(@Nullable MeasurementStream input) {
                return input.isEmpty();
            }
        });
    }

    public boolean isIncomplete() {
        Boolean noStreams = streams.isEmpty();
        Boolean noMeasurements = any(streams.values(), new Predicate<MeasurementStream>() {
            @Override
            public boolean apply(@Nullable MeasurementStream input) {
                return input.isEmpty();
            }
        });

        return noStreams || noMeasurements;
    }

    public void removeStream(MeasurementStream stream) {
        String sensorName = stream.getSensorName();
        streams.remove(sensorName);
    }

    public List<MeasurementStream> getActiveMeasurementStreams() {
        return newArrayList(filter(getMeasurementStreams(), new Predicate<MeasurementStream>() {
            @Override
            public boolean apply(@Nullable MeasurementStream stream) {
                return stream != null && !stream.isMarkedForRemoval() && stream.isVisible();
            }
        }));
    }

    public int getStreamsSize() {
        return getActiveMeasurementStreams().size();
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", markedForRemoval=" + markedForRemoval +
                ", submittedForRemoval=" + submittedForRemoval +
                '}';
    }

    public boolean isLocationless() {
        return locationless;
    }

    public void setLocationless(boolean locationless) {
        this.locationless = locationless;
    }

    public boolean isFixed() {
        if (this.type.equals("FixedSession")) {
            return true;
        } else {
            return false;
        }
    }

    public void setFixed(boolean isFixed) {
        if (isFixed) {
            this.type = "FixedSession";
        } else {
            this.type = "MobileSession";
        }
    }

    public String getType() {
        return this.type;
    }

    public int getDrawable() {
        if (isFixed()) {
            this.drawable = R.drawable.session_fixed_icon;
        } else {
            this.drawable = R.drawable.session_mobile_icon;
        }

        return this.drawable;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIndoor() {
        return isIndoor;
    }

    public void setIndoor(boolean isIndoor) {
        this.isIndoor = isIndoor;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}
