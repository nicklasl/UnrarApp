package nu.nldv.uppackaren.util;

import java.util.List;

import nu.nldv.uppackaren.model.QueueItem;
import nu.nldv.uppackaren.model.RarArchive;
import nu.nldv.uppackaren.model.StatusResponse;
import nu.nldv.uppackaren.model.UnrarResponse;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface RestAPI {

    @GET("/info")
    Response getInfoSynchronous();

    @GET("/{id}")
    void getRarArchives(@Path("id") String id, Callback<List<RarArchive>> callback);

    @POST("/{id}")
    void unRar(@Path("id") String id, Callback<UnrarResponse> callback);

    @GET("/status")
    void getStatus(Callback<StatusResponse> callback);

    @GET("/queue")
    void getQueue(Callback<List<QueueItem>> callback);
}
