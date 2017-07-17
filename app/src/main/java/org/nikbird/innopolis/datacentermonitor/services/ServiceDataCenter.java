package org.nikbird.innopolis.datacentermonitor.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IRack;
import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IServer;
import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IServerRoom;
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
    private boolean mAuthenticated;

    private List<IListener> mSubscribers;
    private boolean mReplicationComplete;
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
                        mReplicationComplete = true;
                    } else
                        mReplicationErrorMsg = mRequestErrorMsg;
                } catch (Exception e) {
                    mReplicationErrorMsg = "Exception: " + e.getMessage();
                }
                finally {
                    mRequestResultLock.readLock().unlock();
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        replicate();
                    }
                }, 10000);
                notifyReplicationComplete();
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

    private String mRequestErrorMsg;
    private int mResponseStatus;
    private List<String> mResponse;
    private final ReadWriteLock mRequestResultLock = new ReentrantReadWriteLock();

    private void httpRequest(final URL url, final String[] requestBody, final Runnable onResult) {
        new Thread(new Runnable() {

            private String mLine;
            private BufferedReader mReader;

            @Override
            public void run() {
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

    private void notifyAuthenticationEvent() {
        for (IListener subscriber : mSubscribers)
            subscriber.onAuthenticationEvent();
    }

    private void notifyReplicationComplete() {
        for (IListener subscriber : mSubscribers) {
            subscriber.onReplicationEvent();
        }
    }
}
