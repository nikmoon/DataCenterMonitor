package org.nikbird.innopolis.datacentermonitor.interfaces;

import android.content.Context;

import java.util.List;

/**
 * Created by nikbird on 11.07.17.
 */

public interface IDataCenter extends Iterable<IServer> {

    interface IListener {
        void onServerStateChanged(IServer server, IServer.State statePrev);
        void onReplicationComplete(IDataCenter dataCenter);
    }

    boolean start(Context context);
    boolean isReplicationComplete();
    void setEventListener(IListener listener);
    void removeEventListener(IListener listener);

    List<IServer> getFailedServers(List<IServer> buffer);
    List<IServer> getFailedServers();

    Context getContext();
    int getRackCount();
    int getRackCapacity();
    int getServerCount();

    void repairServer(IServer server);
}

