package org.nikbird.innopolis.datacentermonitor.abstractclasses;

import android.content.Context;
import android.os.Handler;

import org.nikbird.innopolis.datacentermonitor.LocalDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;

import java.util.ArrayList;
import java.util.HashSet;
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

    private Context mContext;
    private Handler mHandler;

    private Set<IServer> mServers = new HashSet<>();
    private int mRackCount;
    private int mRackCapacity;
    private List<IListener> mListeners;
    private boolean mReplicationComplete = false;

    @Override
    public boolean start(Context context) {
        if (mContext != null)
            return false;
        mContext = context;
        mHandler = new Handler();
        return true;
    }

    @Override
    public boolean isReplicationComplete() {
        return mReplicationComplete;
    }

    protected void notifyReplicationComplete() {
        mReplicationComplete = true;
        for(IListener listener: mListeners)
            listener.onReplicationComplete(this);
    }

    protected void postNotification(Runnable action) {
        mHandler.post(action);
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
}
