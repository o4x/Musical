package com.o4x.musical.network.service;

import com.o4x.musical.network.Models.DeezerArtistModel;
import com.o4x.musical.network.Models.DeezerModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DeezerService {
    String DEEZER_API_URL = "https://api.deezer.com/";

    @GET(DEEZER_API_URL+"search?")
    Call<DeezerModel> searchDeezer(@Query("q") String name);

    @GET(DEEZER_API_URL+"search/artist?")
    Call<DeezerArtistModel> searchDeezerArtist(@Query("q") String artistName);
}
