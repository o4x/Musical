package com.o4x.musical.network.temp.Lastfmapi.Models;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ITunesResultModel {


    @SerializedName("resultCount")
    public int resultCount;

    @SerializedName("results")
    public List<Results> results;

    public static class Results implements Serializable {
        @SerializedName("wrapperType")
        public String wrapperType;
        @SerializedName("kind")
        public String kind;
        @SerializedName("artistId")
        public int artistId;
        @SerializedName("collectionId")
        public int collectionId;
        @SerializedName("trackId")
        public int trackId;
        @SerializedName("artistName")
        public String artistName;
        @SerializedName("collectionName")
        public String collectionName;
        @SerializedName("trackName")
        public String trackName;
        @SerializedName("collectionCensoredName")
        public String collectionCensoredName;
        @SerializedName("trackCensoredName")
        public String trackCensoredName;
        @SerializedName("artistViewUrl")
        public String artistViewUrl;
        @SerializedName("collectionViewUrl")
        public String collectionViewUrl;
        @SerializedName("trackViewUrl")
        public String trackViewUrl;
        @SerializedName("previewUrl")
        public String previewUrl;
        @SerializedName("artworkUrl30")
        public String artworkUrl30;
        @SerializedName("artworkUrl60")
        public String artworkUrl60;
        @SerializedName("artworkUrl100")
        public String artworkUrl100;
        @SerializedName("collectionPrice")
        public double collectionPrice;
        @SerializedName("trackPrice")
        public double trackPrice;
        @SerializedName("releaseDate")
        public String releaseDate;
        @SerializedName("collectionExplicitness")
        public String collectionExplicitness;
        @SerializedName("trackExplicitness")
        public String trackExplicitness;
        @SerializedName("discCount")
        public int discCount;
        @SerializedName("discNumber")
        public int discNumber;
        @SerializedName("trackCount")
        public int trackCount;
        @SerializedName("trackNumber")
        public int trackNumber;
        @SerializedName("trackTimeMillis")
        public int trackTimeMillis;
        @SerializedName("country")
        public String country;
        @SerializedName("currency")
        public String currency;
        @SerializedName("primaryGenreName")
        public String primaryGenreName;


        public String getBigArtworkUrl() {
            return artworkUrl100
                    .replace("100x100", "1000x1000");
        }


        public String getYear() {
            return releaseDate.split("-")[0];
        }
    }
}
