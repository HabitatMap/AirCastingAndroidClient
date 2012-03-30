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

import pl.llp.aircasting.helper.SoundHelper;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class Session implements Serializable
{
    @Expose private UUID uuid = UUID.randomUUID();

    @Expose @SerializedName("measurements") transient List<Measurement> measurements = newArrayList();
    @Expose @SerializedName("streams") transient Map<String, MeasurementStream> streams = newHashMap();
    @Expose private List<Note> notes = newArrayList();

    @Expose private String title;
    @Expose @SerializedName("tag_list") private String tags;
    @Expose private String description;
    @Expose private int calibration;
    @Expose private boolean contribute;
    @Expose @SerializedName("os_version") private String osVersion;
    @Expose @SerializedName("data_type") private String dataType;
    @Expose private String instrument;
    @Expose @SerializedName("phone_model") private String phoneModel;
    @Expose @SerializedName("offset_60_db") private int offset60DB;
    @Expose private String location;
    @Expose @SerializedName("deleted") private boolean markedForRemoval;

    private Double sum;
    private Double peak;
    private Date end;
    private Date start;
    private Long id = null;
    private boolean submittedForRemoval = false;

  public void add(MeasurementStream stream)
  {
    streams.put(stream.getSensorName(), stream);
  }

  public void add(Measurement measurement) {
        if (measurements.isEmpty()) {
            // If average has been set earlier manually
            sum = measurement.getValue();
        } else {
            sum += measurement.getValue();
        }
        if (peak == null || measurement.getValue() > peak) {
            peak = measurement.getValue();
        }

        measurements.add(measurement);
    }

    public List<Measurement> getMeasurements() {
        if (measurements == null) {
            measurements = newArrayList();
        }
        return measurements;
    }

  public void setTitle(String text) {
        title = text;
    }

    public void setTags(String text) {
        tags = text;
    }

    public void setDescription(String text) {
        description = text;
    }

    public String getTitle() {
        return title;
    }

    public String getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public double getPeak() {
        if (peak == null) {
            peak = calculatePeak();
        }
        return peak;
      }

    private double calculatePeak() {
        double newPeak = SoundHelper.TOTALLY_QUIET;
        for (Measurement measurement : measurements) {
            if (measurement.getValue() > newPeak) {
                newPeak = measurement.getValue();
    }
        }
        return newPeak;
    }

    public double getAvg() {
        if (sum == null) {
            sum = calculateSum();
        }
        return sum / (measurements.isEmpty() ? 1 : measurements.size());
    }

    private double calculateSum() {
        if (measurements.isEmpty()) return SoundHelper.TOTALLY_QUIET;

        double newSum = 0;
        for (Measurement measurement : measurements) {
            newSum += measurement.getValue();
        }
        return newSum;
    }

    public void setAvg(double avg) {
        this.sum = avg;
    }

    public void setPeak(double peak) {
        this.peak = peak;
    }

  public Date getEnd() {
        if (end == null) {
            end = getLast(measurements).getTime();
      }
        return end;
    }

    public Date getStart() {
        if (start == null) {
            start = measurements.get(0).getTime();
  }
        return start;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public boolean isSaved() {
    return id != null;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public Long getId()
  {
    return id;
  }

  public void updateHeader(Session session)
  {
    this.title = session.getTitle();
    this.tags = session.getTags();
    this.description = session.getDescription();
  }

  public List<Note> getNotes()
  {
    return notes;
  }

  public void add(Note note)
  {
    notes.add(note);
  }

  public void addAll(Collection<Note> notesToAdd)
  {
    notes.addAll(notesToAdd);
  }

  public void setUuid(UUID uuid)
  {
    this.uuid = uuid;
  }

  public UUID getUUID()
  {
    return uuid;
  }

  public void setLocation(String location)
  {
    this.location = location;
  }

  public String getLocation()
  {
    return location;
  }

  public int getCalibration()
  {
    return calibration;
  }

  public void setCalibration(int calibration)
  {
    this.calibration = calibration;
  }

  public void setContribute(boolean contribute)
  {
    this.contribute = contribute;
  }

  public boolean getContribute()
  {
    return contribute;
  }

  public String getOSVersion()
  {
    return osVersion;
  }

  public String getDataType()
  {
    return dataType;
  }

  public String getInstrument()
  {
    return instrument;
  }

  public String getPhoneModel()
  {
    return phoneModel;
  }

  public void setOsVersion(String osVersion)
  {
    this.osVersion = osVersion;
  }

  public void setDataType(String dataType)
  {
    this.dataType = dataType;
  }

  public void setInstrument(String instrument)
  {
    this.instrument = instrument;
  }

  public void setPhoneModel(String phoneModel)
  {
    this.phoneModel = phoneModel;
  }

  public int getOffset60DB()
  {
    return offset60DB;
  }

  public void setOffset60DB(int offset60DB)
  {
    this.offset60DB = offset60DB;
  }

  public boolean isMarkedForRemoval()
  {
    return markedForRemoval;
  }

  public void setMarkedForRemoval(boolean markedForRemoval)
  {
    this.markedForRemoval = markedForRemoval;
  }

  public void deleteNote(Note note)
  {
    notes.remove(note);
  }

  public void setSubmittedForRemoval(boolean submittedForRemoval)
  {
    this.submittedForRemoval = submittedForRemoval;
  }

  public boolean isSubmittedForRemoval()
  {
    return submittedForRemoval;
  }

  public Collection<MeasurementStream> getMeasurementStreams()
  {
    return streams.values();
  }
  
  public MeasurementStream getStream(String name)
  {
    return streams.get(name);
  }
}
