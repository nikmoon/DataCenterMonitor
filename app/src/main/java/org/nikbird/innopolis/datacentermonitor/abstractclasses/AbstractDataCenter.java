package org.nikbird.innopolis.datacentermonitor.abstractclasses;

import android.content.Context;

import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nikbird on 11.07.17.
 */

public abstract class AbstractDataCenter implements IDataCenter {

    private Context mContext;

    private Set<IServer> mServers = new HashSet<>();
    private int mRackCount;
    private int mRackCapacity;
    private IListener mListener;
    private boolean mReplicationComplete = false;

    @Override
    public synchronized boolean startDataCenter(Context context) {
        if (mContext != null)
            return false;
        mContext = context;
        return true;
    }

    @Override
    public boolean isReplicationComplete() {
        return mReplicationComplete;
    }

    protected void setReplicationComplete() {
        mReplicationComplete = true;
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
    public int getRackCount() {
        return mRackCount;
    }

    protected void setRackCount(int rackCount) {
        mRackCount = rackCount;
    }

    @Override
    public void setEventListener(IListener listener) {
        mListener = listener;
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
