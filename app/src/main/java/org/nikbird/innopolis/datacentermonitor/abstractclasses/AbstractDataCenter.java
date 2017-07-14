package org.nikbird.innopolis.datacentermonitor.abstractclasses;

import android.app.Service;
import android.content.Context;
import android.os.Handler;

import org.nikbird.innopolis.datacentermonitor.LocalDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by nikbird on 11.07.17.
 */

public abstract class AbstractDataCenter implements IDataCenter {

    private static IDataCenter sDataCenter = LocalDataCenter.getInstance();
    public static final IDataCenter getDataCenter() { return sDataCenter; }
    public static final void setDataCenter(IDataCenter dataCenter) {
        sDataCenter = dataCenter;
    }

    public static final String SERVICE_TASK_TAG = "datacentermonitor.servicetask";

    private Context mContext;
    private Handler mHandler;

    private Set<IServer> mServers;
    private int mRackCount;
    private int mRackCapacity;
    private List<IListener> mListeners;
    private boolean mReplicationComplete = false;

    public enum ServiceTask {
        DELIVERY_STRUCTURE,
        DELIVERY_SERVER_EVENT
    }


    @Override
    public boolean start(Context context) {
        if (mContext != null)
            return false;
        mContext = context;
        mHandler = new Handler();
        mServers = new HashSet<>();
        mListeners = new ArrayList<>();
        return true;
    }

    @Override
    public boolean isReplicationComplete() {
        return mReplicationComplete;
    }


    public abstract void deliveryDataCenterInfo(int rackCount, int rackCapacity, int[] rackNums, int[] serverNums, IServer.State[] states);

    protected void notifyReplicationComplete() {
        mReplicationComplete = true;
        for(IListener listener: mListeners)
            listener.onReplicationComplete(this);
    }

    protected void notifyServerStateChanged(IServer server, IServer.State prevState) {
        for(IListener listener: mListeners) {
            listener.onServerStateChanged(server, prevState);
        }
    }

    protected void postNotification(Runnable action) {
        mHandler.post(action);
    }
    protected void postNotification(Runnable action, long delayMillis) {
        mHandler.postDelayed(action, delayMillis);
    }

    protected Set<IServer> getServers() {
        return mServers;
    }

    protected void initServers(Set<IServer> servers) {
        mServers = servers;
    }

    protected void addServer(IServer server) {
        mServers.add(server);
    }

    @Override
    public List<IServer> getFailedServers(List<IServer> failedServers) {
        for(IServer server: getServers()) {
            if (server.getState() == IServer.State.FAIL)
                failedServers.add(server);
        }
        return failedServers;
    }

    @Override
    public List<IServer> getFailedServers() {
        return getFailedServers(new ArrayList<IServer>());
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public int getRackCount() {
        return mRackCount;
    }

    protected void setRackCount(int rackCount) {
        mRackCount = rackCount;
    }

    @Override
    public void setEventListener(IListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void removeEventListener(IListener listener) {
        for (int i = 0, count = mListeners.size(); i < count; i++)
            if (mListeners.get(i) == listener) {
                mListeners.remove(i);
                break;
            }
    }

    @Override
    public int getRackCapacity() {
        return mRackCapacity;
    }

    protected void setRackCapacity(int rackCapacity) {
        mRackCapacity = rackCapacity;
    }

    @Override
    public int getServerCount() {
        return mServers.size();
    }

    @Override
    public Iterator<IServer> iterator() {
        return mServers.iterator();
    }
}
