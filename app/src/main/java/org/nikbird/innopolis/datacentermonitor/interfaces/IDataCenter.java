package org.nikbird.innopolis.datacentermonitor.interfaces;

import android.content.Context;

/**
 * Created by nikbird on 11.07.17.
 */

public interface IDataCenter {

    interface IListener {
        void onServerStateChanged(IServer server, IServer.State statePrev);
        void onReplicationComplete(IDataCenter dataCenter);
    }

    boolean startDataCenter(Context context);
    boolean isReplicationComplete();
    void setEventListener(IListener listener);

    IServer[] getFailedServers();

    int getRackCount();
    int getRackCapacity();
    int getServerCount();

    void repairServer(IServer server);
}

