package com.o4x.musical.network.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DeezerArtistModel {

    @SerializedName("data")
    public List<Data> data;
    @SerializedName("total")
    public int total;
    @SerializedName("next")
    public String next;

    public static class Data implements Serializable {
        @SerializedName("id")
        public String id;
        @SerializedName("name")
        public String name;
        @SerializedName("link")
        public String link;
        @SerializedName("picture")
        public String picture;
        @SerializedName("picture_small")
        public String pictureSmall;
        @SerializedName("picture_medium")
        public String pictureMedium;
        @SerializedName("picture_big")
        public String pictureBig;
        @SerializedName("picture_xl")
        public String pictureXl;
        @SerializedName("nb_album")
        public int nbAlbum;
        @SerializedName("nb_fan")
        public int nbFan;
        @SerializedName("radio")
        public boolean radio;
        @SerializedName("tracklist")
        public String tracklist;
        @SerializedName("type")
        public String type;
    }


}
