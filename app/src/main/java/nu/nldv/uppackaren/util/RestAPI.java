package nu.nldv.uppackaren.util;

import java.util.List;

import nu.nldv.uppackaren.model.RarArchive;
import nu.nldv.uppackaren.model.UnrarResponse;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface RestAPI {

    @GET("/info")
    Response getInfoSynchronous();

    @GET("/")
    void getRarArchives(Callback<List<RarArchive>> callback);

    @POST("/{id}")
    void unRar(@Path("id") String id, Callback<UnrarResponse> callback);
}
