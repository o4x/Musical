package com.o4x.musical.network.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DeezerModel {

    @SerializedName("data")
    public List<Data> data;
    @SerializedName("total")
    public int total;
    @SerializedName("next")
    public String next;


    public static class Data implements Serializable {
        @SerializedName("id")
        public int id;
        @SerializedName("readable")
        public boolean readable;
        @SerializedName("title")
        public String title;
        @SerializedName("title_short")
        public String title_short;
        @SerializedName("link")
        public String link;
        @SerializedName("duration")
        public int duration;
        @SerializedName("rank")
        public int rank;
        @SerializedName("explicit_lyrics")
        public boolean explicit_lyrics;
        @SerializedName("explicit_content_lyrics")
        public int explicit_content_lyrics;
        @SerializedName("explicit_content_cover")
        public int explicit_content_cover;
        @SerializedName("preview")
        public String preview;
        @SerializedName("artist")
        public Artist artist;
        @SerializedName("album")
        public Album album;
        @SerializedName("type")
        public String type;


        public static class Artist {
            @SerializedName("id")
            public int id;
            @SerializedName("name")
            public String name;
            @SerializedName("link")
            public String link;
            @SerializedName("picture")
            public String picture;
            @SerializedName("picture_small")
            public String picture_small;
            @SerializedName("picture_medium")
            public String picture_medium;
            @SerializedName("picture_big")
            public String picture_big;
            @SerializedName("picture_xl")
            public String picture_xl;
            @SerializedName("tracklist")
            public String tracklist;
            @SerializedName("type")
            public String type;
        }

        public static class Album {
            @SerializedName("id")
            public int id;
            @SerializedName("title")
            public String title;
            @SerializedName("cover")
            public String cover;
            @SerializedName("cover_small")
            public String cover_small;
            @SerializedName("cover_medium")
            public String cover_medium;
            @SerializedName("cover_big")
            public String cover_big;
            @SerializedName("cover_xl")
            public String cover_xl;
            @SerializedName("tracklist")
            public String tracklist;
            @SerializedName("type")
            public String type;
        }
    }
}
