package org.nikbird.innopolis.datacentermonitor.interfaces.newgen;

import android.content.Context;

import java.util.Iterator;
import java.util.List;

/**
 * Created by nikbird on 11.07.17.
 */

public interface IDataCenter {

    interface IListener {
        void onServerAdded(IServer server);
        void onServerRemoved(IServer server, IServer.ServerPosition prevPosition);
        void onServerStateChanged(IServer server, IServer.State prevState);
        void onReplicationComplete();
    }

    void setEventListener(IListener listener);
    void removeEventListener(IListener listener);

    boolean isAuthenticated();
    boolean isReplicationComplete();
    boolean hasProblem();

    Iterator<IRack> rackIterator();
    void authentication(Runnable onResult);
}