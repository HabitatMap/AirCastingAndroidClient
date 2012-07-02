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

import android.location.Location;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;

public class Note implements Serializable
{
    @Expose private Date date;
    @Expose private String text;
    @Expose private double latitude;
    @Expose private double longitude;
    @Expose private int number;
    @Expose @SerializedName("photo_location") private String photoPath;

    public Note() {
    }

  public Note(Date date, String text, @Nullable Location location, String picturePath)
  {
    this.date = date;
    this.text = text;
    this.photoPath = picturePath;
    if (location != null)
    {
      this.latitude = location.getLatitude();
      this.longitude = location.getLongitude();
    }
  }

  public Note(Date date, String text, @Nullable Location location, String picturePath, int number)
  {
    this.date = date;
    this.text = text;
    this.photoPath = picturePath;
    if (location != null)
    {
      this.latitude = location.getLatitude();
      this.longitude = location.getLongitude();
    }
    this.number = number;
  }

  @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        if (Double.compare(note.latitude, latitude) != 0) return false;
        if (Double.compare(note.longitude, longitude) != 0) return false;
        if (number != note.number) return false;
        if (date != null ? !date.equals(note.date) : note.date != null) return false;
        if (photoPath != null ? !photoPath.equals(note.photoPath) : note.photoPath != null) return false;
        if (text != null ? !text.equals(note.text) : note.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = date != null ? date.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        temp = latitude != +0.0d ? Double.doubleToLongBits(latitude) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = longitude != +0.0d ? Double.doubleToLongBits(longitude) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (photoPath != null ? photoPath.hashCode() : 0);
        result = 31 * result + number;
        return result;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String picturePath) {
        this.photoPath = picturePath;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Note{" +
                "date=" + date +
                ", text='" + text + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", photoPath='" + photoPath + '\'' +
                ", number=" + number +
                '}';
    }
}
