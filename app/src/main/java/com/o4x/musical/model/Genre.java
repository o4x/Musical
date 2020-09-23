package com.o4x.musical.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Genre implements Parcelable {

    public final int id;
    public final String name;
    public final int songCount;
    public final List<Song> songs;

    public Genre(final int id, final String name, final int songCount, final List<Song> songs) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
        this.songs = songs;
    }

    public Genre(final int id, final String name, final int songCount) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
        this.songs = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genre genre = (Genre) o;

        if (id != genre.id) return false;
        if (!name.equals(genre.name)) return false;
        if (!songs.equals(genre.songs)) return false;
        return songCount == genre.songCount;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + songCount;
        result = 31 * result + (songs != null ? songs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", songCount=" + songCount + '\'' +
                ", songs=" + songs +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.songCount);
        dest.writeTypedList(this.songs);
    }

    protected Genre(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.songCount = in.readInt();
        this.songs = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        public Genre createFromParcel(Parcel source) {
            return new Genre( source);
        }

        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };
}
