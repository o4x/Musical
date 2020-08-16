package com.o4x.musical.network.temp.Lastfmapi;



import com.o4x.musical.network.temp.Lastfmapi.Models.AlbumModel;
import com.o4x.musical.network.temp.Lastfmapi.Models.ArtistModel;
import com.o4x.musical.network.temp.Lastfmapi.Models.ITunesModel;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by REYANSH on 3/13/2017.
 */

/**
 * for more information please check :
 * https://affiliate.itunes.apple.com/resources/documentation/itunes-store-web-service-search-api/
 */
public interface ITunesService {

    String ITUNES_API_URL = "https://itunes.apple.com/search?";

    String ENTITY_TRACK = "musicTrack";
    String ENTITY_ALBUM = "album";
    String ENTITY_ARTIST = "musicArtist";

    @POST(ApiClient.BASE_PARAMETERS_ARTIST)
    Call<ArtistModel> getArtist(@Query("artist") String artistName);

    @POST(ApiClient.BASE_PARAMETERS_ALBUM)
    Call<AlbumModel> getAlbum(@Query("album") String albumName, @Query("artist") String artistName);

    @POST(ITUNES_API_URL)
    Call<ITunesModel> searchITunes(
            @Query("term") String termName,
            @Query("entity") String entityName
    );
}
