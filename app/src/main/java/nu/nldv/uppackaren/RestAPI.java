package nu.nldv.uppackaren;

import java.util.List;

import nu.nldv.uppackaren.model.RarArchive;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface RestAPI {
    @GET("/")
    void getRarArchives(Callback<List<RarArchive>> callback);

    @POST("/{id}")
    void unRar(@Path("id") String id, Callback<String> callback);
}
