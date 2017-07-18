package org.nikbird.innopolis.datacentermonitor.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IRack;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServerRoom;
import org.nikbird.innopolis.datacentermonitor.models.Rack;
import org.nikbird.innopolis.datacentermonitor.models.Server;
import org.nikbird.innopolis.datacentermonitor.models.ServerRoom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServiceDataCenter extends Service implements IDataCenter {

    public class LocalBinder extends Binder {
        public IDataCenter getDataCenter() { return ServiceDataCenter.this; }
    }

    private Handler mHandler;
    private final IBinder mBinder = new LocalBinder();
    private URL mUrl;
    private String mAuthToken;
    private volatile boolean mAuthenticated;

    private List<IListener> mSubscribers;
    private volatile boolean mReplicationComplete;
    private List<IServer> mProblemServers;

    private IServerRoom mServerRoom;

    @Override public void onCreate() {
        super.onCreate();

        mHandler = new Handler();
        mSubscribers = new ArrayList<>();
        mProblemServers = new ArrayList<>();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override public boolean onUnbind(Intent intent) {
        mInterruptWaiting = true;
        Toast.makeText(this, "Waiting thread will stopped", Toast.LENGTH_SHORT);
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override public void onDestroy() {
        super.onDestroy();
    }

    @Override public void setEventListener(IListener listener) {
        if (listener == null)
            throw new NullPointerException();
        for (IListener subscriber : mSubscribers) {
            if (subscriber == listener)
                return;
        }
        mSubscribers.add(listener);
    }

    @Override public void removeEventListener(IListener listener) {
        for (int i = 0; i < mSubscribers.size(); i++)
            if (mSubscribers.get(i) == listener) {
                mSubscribers.remove(i);
                return;
            }
    }

    private String mAuthErrorMessage;
    @Override public String authErrorMessage() { return mAuthErrorMessage; }

    @Override public void resetAuthentication() {
        mAuthToken = "";
        mAuthenticated = false;
        mInterruptWaiting = true;
    }

    @Override public void authentication(String username, String password, String urlString) {
        mAuthenticated = false;
        mAuthErrorMessage = null;
        URL urlAuth;
        try {
            mUrl = new URL(urlString);
            urlAuth = new URL(mUrl, "/auth");
        } catch (MalformedURLException e) {
            mAuthErrorMessage = "URL invalid";
            notifyAuthenticationEvent();
            return;
        }
        Runnable onAuthResult = new Runnable() {
            @Override public void run() {
                mRequestResultLock.readLock().lock();
                try {
                    String responseString;
                    if (mRequestErrorMsg == null) {
                        responseString = mResponse.get(0);
                        if (responseString.startsWith("token:")) {
                            mAuthToken = responseString.split(":")[1].trim();
                            mAuthenticated = true;
                            if (!mReplicationInProgress)
                                replicate();
                        } else {
                            mAuthErrorMessage = responseString;
                        }
                    } else {
                        mAuthErrorMessage = mRequestErrorMsg;
                    }
                } catch (Exception e) {
                    mAuthErrorMessage = "Error parsing server response";
                } finally {
                    mRequestResultLock.readLock().unlock();
                }
                notifyAuthenticationEvent();
            }
        };
        if ("http".equals(mUrl.getProtocol()))
            httpRequest(urlAuth, new String[] {"username:" + username, "password:" + password}, onAuthResult);
        else {
            mAuthErrorMessage = "Unknown URL protocol";
            notifyAuthenticationEvent();
        }
    }

    private boolean mReplicationInProgress;
    private String mReplicationErrorMsg;
    @Override public String replicationErrorMessage() { return mReplicationErrorMsg; }

    private void replicate() {
        mReplicationInProgress = true;
        mReplicationComplete = false;
        mReplicationErrorMsg = null;

        Runnable onResult = new Runnable() {
            @Override
            public void run() {
                mRequestResultLock.readLock().lock();
                try {
                    if (mRequestErrorMsg == null) {
                        mServerRoom = new ServerRoom(Integer.valueOf(mResponse.get(0).split(":")[1]));
                        for(int i = 1; i < mResponse.size(); i++) {
                            String line = mResponse.get(i);
                            if (line.startsWith("rack")) {
                                String[] values = line.split(":");
                                IRack rack = new Rack(Integer.valueOf(values[2]));
                                if (!mServerRoom.insertRack(rack, Integer.valueOf(values[1])))
                                    throw new Exception("cant insert rack in server room");
                                for(int j = 0; j < rack.capacity(); j++) {
                                    i++;
                                    line = mResponse.get(i);
                                    Server server = new Server(IServer.State.valueOf(line));
                                    if (!rack.insertServer(server, j))
                                        throw new Exception("cant insert server in rack");
                                }
                            }
                        }
                        mReplicationComplete = true;
                        mReplicationInProgress = false;

                        if (!mWaitInProgress)
                            waitRemoteEvent();
                    } else
                        mReplicationErrorMsg = mRequestErrorMsg;
                } catch (Exception e) {
                    mReplicationErrorMsg = "Exception: " + e.getMessage();
                }
                finally {
                    mRequestResultLock.readLock().unlock();
                }
                if (mReplicationErrorMsg != null)
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            replicate();
                        }
                    }, 10000);
                notifyReplicationEvent();
            }
        };
        if ("http".equals(mUrl.getProtocol())) {
            httpRequest(mUrl, new String[] {"token:" + mAuthToken}, onResult);
        }
    }

    @Override public boolean isAuthenticated() { return mAuthenticated; }
    @Override public boolean isReplicationComplete() { return mReplicationComplete; }
    @Override public boolean hasProblem() { return mProblemServers.size() > 0; }
    @Override public List<IServer> getProblemServers() { return mProblemServers; }
    @Override public Iterable<IRack> rackIterable() { return mServerRoom; }
    @Override public int countRacks() { return mServerRoom.countRacks(); }

    private String mRequestErrorMsg;
    private int mResponseStatus;
    private List<String> mResponse;
    private final ReadWriteLock mRequestResultLock = new ReentrantReadWriteLock();

    private void httpRequest(final URL url, final String[] requestBody, final Runnable onResult) {
        new Thread(new Runnable() {

            private String mLine;
            private BufferedReader mReader;

            @Override public void run() {
                mRequestErrorMsg = null;
                try {
                    mRequestResultLock.writeLock().lock();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if (requestBody != null && requestBody.length > 0) {
                        conn.setDoOutput(true);
                        Writer writer = new OutputStreamWriter(conn.getOutputStream());
                        for (int i = 0; i < requestBody.length; i++)
                            writer.write(requestBody[i] + "\n");
                        writer.close();
                    }

                    mResponseStatus = conn.getResponseCode();
                    mReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    mResponse = new ArrayList<>();
                    while ((mLine = mReader.readLine()) != null) {
                        mResponse.add(mLine);
                    }
                } catch (IOException e) {
                    mRequestErrorMsg = "IOException: " + e.getMessage();
                } catch (Exception e) {
                    mRequestErrorMsg = "Exception: " + e.getMessage();
                } finally {
                    mRequestResultLock.writeLock().unlock();
                    mHandler.post(onResult);
                }
            }
        }).start();
    }


    private BlockingQueue<String> mEventQueue = new ArrayBlockingQueue<String>(10);

    private volatile boolean mWaitInProgress;
    private volatile boolean mInterruptWaiting;
    private URL mWaitEventUrl;

    private void onEvent() {
        String event;
        try {
            event = mEventQueue.take();
        } catch (InterruptedException e) {
            throw new Error("Event queue interrupted");
        }

        if (event.startsWith("error:") || event.startsWith("clienterror:")) {
            Toast.makeText(this, event, Toast.LENGTH_SHORT).show();
        }
        else {
            String[] eventParams = event.split(":");
            switch (eventParams[0]) {
                case "server":
                    int rackNum = Integer.valueOf(eventParams[1]);
                    int serverNum = Integer.valueOf(eventParams[2]);
                    IServer.State state = IServer.State.valueOf(eventParams[3]);
                    IRack rack = mServerRoom.getRack(rackNum);
                    IServer server = rack.getServer(serverNum);
                    IServer.State prevState = server.state();
                    server.setState(state);
                    notifyServerStateChanged(server, prevState);
                    break;
            }
        }

        if (!mWaitInProgress)
            waitRemoteEvent();
    }

    public static final int WAIT_DURATION = 5000;

    private void waitRemoteEvent() {
        if (mWaitEventUrl == null)
            try {
                mWaitEventUrl= new URL(mUrl, "/event");
            } catch (MalformedURLException e) {
                try {
                    mEventQueue.put("clienterror:URL exception: " + e.toString());
                    mHandler.post(new Runnable() { @Override public void run() { onEvent(); } });
                } catch (InterruptedException e1) {
                    throw new Error("Event queue interrupted");
                }
                return;
            }

        new Thread(new Runnable() {
            @Override public void run() {
                mWaitInProgress = true;
                while (true) {
                    if (mInterruptWaiting) {
                        mWaitInProgress = false;
                        break;
                    }

                    long startWaiting = System.currentTimeMillis();
                    try {
                        HttpURLConnection conn = (HttpURLConnection) mWaitEventUrl.openConnection();

                        conn.setDoOutput(true);
                        Writer writer = new OutputStreamWriter(conn.getOutputStream());
                        writer.write("token:" + mAuthToken + "\n");
                        writer.close();

                        int responseStatus = conn.getResponseCode();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String event = reader.readLine();
                        try {
                            mEventQueue.put(event);
                            mHandler.post(new Runnable() { @Override public void run() { onEvent(); } });
                        } catch (InterruptedException e1) {
                            throw new Error("Event queue interrupted");
                        }
                        if (event.startsWith("servererror:")) {
                            long waitDuration = System.currentTimeMillis() - startWaiting;
                            if (waitDuration < WAIT_DURATION)
                                try {
                                    Thread.sleep(WAIT_DURATION - waitDuration);
                                } catch (InterruptedException e1) {
                                }
                        }
                    } catch (IOException e) {
                        try {
                            mEventQueue.put("clienterror:" + e.toString());
                            mHandler.post(new Runnable() { @Override public void run() { onEvent(); } });
                        } catch (InterruptedException e1) {
                            throw new Error("Event queue interrupted");
                        }
                        long waitDuration = System.currentTimeMillis() - startWaiting;
                        if (waitDuration < WAIT_DURATION) {
                            try {
                                Thread.sleep(WAIT_DURATION - waitDuration);
                            } catch (InterruptedException e1) {
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void notifyAuthenticationEvent() {
        for (IListener subscriber : mSubscribers)
            subscriber.onAuthenticationEvent();
    }

    private void notifyReplicationEvent() {
        for (IListener subscriber : mSubscribers) {
            subscriber.onReplicationEvent();
        }
    }

    private void notifyServerStateChanged(IServer server, IServer.State prevState) {
        for (IListener subscriber : mSubscribers) {
            subscriber.onServerStateChanged(server, prevState);
        }
    }
}
