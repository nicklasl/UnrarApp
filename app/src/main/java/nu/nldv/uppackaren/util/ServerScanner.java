package nu.nldv.uppackaren.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nu.nldv.uppackaren.UppackarenApplication;
import nu.nldv.uppackaren.model.Server;

public class ServerScanner extends AsyncTask<Integer, Integer, List<Server>> {

    private static final String TAG = ServerScanner.class.getSimpleName();
    private final ServerScannerCallback callback;
    private final String subnet;
    private final static int MIN_IP = 1;
    private final static int MAX_IP = 255;
    private final static int NUMBER_OF_THREADS = 20;
    public static final int TIMEOUT = 1000;

    public ServerScanner(String subnet, ServerScannerCallback serverScannerCallback) {
        this.subnet = subnet;
        this.callback = serverScannerCallback;
    }

    @Override
    protected List<Server> doInBackground(Integer... ports) {
        List<Server> servers = new ArrayList<>();
        for (Integer port : ports) {
            if (port != null) {
                try {
                    servers.addAll(scanSubnetOnPort(port));
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return servers;
    }

    private Collection<? extends Server> scanSubnetOnPort(final Integer port) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        Collection<Server> availableServers = getAvailableServers(port);
        Log.i(TAG, "The scanning took: " + (System.currentTimeMillis() - start) / 1000 + " seconds");
        return availableServers;
    }

    private void checkHostSupport(Integer port, Map<Integer, Server> supportedServers, String reacheableHost) {

        RestAPI restAPI = UppackarenApplication.restAPIForEndPoint("http://"+reacheableHost+":"+port);
        try {
            restAPI.getInfoSynchronous();
            Log.i(TAG, reacheableHost + ":" + port + " is supported");
            Server server = new Server(reacheableHost, port);
            supportedServers.put(server.hashCode(), server);
        } catch (Exception e) {
            Log.d(TAG, reacheableHost + ":" + port + " is not supported");
        }
    }

    private Collection<Server> getAvailableServers(final int port) throws IOException, InterruptedException {
        final Map<Integer, Object> completedThreads = new ConcurrentHashMap<>();
        final Map<Integer, Server> supported = new ConcurrentHashMap<>();
        int tick = MAX_IP / NUMBER_OF_THREADS;
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final int start = MIN_IP + (i * tick);
            final int end = MIN_IP + (i * tick) + tick;
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    for (int j = start; j < end; j++) {
                        String host = subnet + "." + j;
                        try {
                            if (InetAddress.getByName(host).isReachable(TIMEOUT)) {
                                Log.i(TAG, host + " is reachable");
                                checkHostSupport(port, supported, host);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    completedThreads.put(this.hashCode(), new Object());
                }
            });
            t.start();
        }

        while (completedThreads.size() < NUMBER_OF_THREADS) {
            Thread.sleep(100);
        }
        return supported.values();
    }

    @Override
    protected void onPostExecute(List<Server> servers) {
        super.onPostExecute(servers);
        callback.callback(servers);
    }
}

