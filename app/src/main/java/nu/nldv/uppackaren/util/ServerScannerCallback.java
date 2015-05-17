package nu.nldv.uppackaren.util;

import java.util.List;

import nu.nldv.uppackaren.model.Server;

public interface ServerScannerCallback {
    void callback(List<Server> servers);
}
