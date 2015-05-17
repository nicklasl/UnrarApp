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

import nu.nldv.uppackaren.model.Server;
import retrofit.RestAdapter;

public class ServerScanner extends AsyncTask<Integer, Integer, List<Server>> {

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
                    Log.e("Uppackaren", e.getMessage());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Log.e("Uppackaren", e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return servers;
    }

    private Collection<? extends Server> scanSubnetOnPort(final Integer port) throws IOException, InterruptedException {
        final List<Server> availableServers = new ArrayList<>();
        long start = System.currentTimeMillis();
        Collection<String> reacheableHosts = getReachableHosts();

        for (final String reacheableHost : reacheableHosts) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("http://" + reacheableHost + ":" + port)
                    .setLogLevel(RestAdapter.LogLevel.NONE)
                    .build();
            try {
                restAdapter.create(RestAPI.class).getInfoSynchronous();
                Log.d("Uppackaren", reacheableHost + ":" + port + " is supported");
                availableServers.add(new Server(reacheableHost, port));
            } catch (Exception e) {
                Log.d("Uppackaren", reacheableHost + ":" + port + " is not supported");
            }
        }

        return availableServers;
    }

    private Collection<String> getReachableHosts() throws IOException, InterruptedException {
        final Map<Integer, String> reachable = new ConcurrentHashMap<>();
        Map completedThreads = new ConcurrentHashMap();
        List<Thread> threads = generateThreads(reachable, completedThreads);
        for (Thread thread : threads) {
            thread.start();
        }

        while (completedThreads.size() < NUMBER_OF_THREADS) {
            Thread.sleep(100);
        }
        return reachable.values();
    }

    private List<Thread> generateThreads(final Map<Integer, String> reachable, final Map completedThreads) {
        List<Thread> threads = new ArrayList<>();
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
                                Log.i("Uppackaren", host + " is reachable");
                                reachable.put(host.hashCode(), host);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    completedThreads.put(this.hashCode(), new Object());
                }
            });
            threads.add(t);
        }
        return threads;
    }

    @Override
    protected void onPostExecute(List<Server> servers) {
        super.onPostExecute(servers);
        callback.callback(servers);
    }
}

