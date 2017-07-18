package org.nikbird.innopolis.datacentermonitor.interfaces;

import java.util.List;

/**
 * Created by nikbird on 11.07.17.
 */

public interface IDataCenter {

    interface IListener {
        void onAuthenticationEvent();
        void onServerAdded(IServer server);
        void onServerRemoved(IServer server, IServer.ServerPosition prevPosition);
        void onServerStateChanged(IServer server, IServer.State prevState);
        void onReplicationEvent();
    }

    void authentication(String username, String password, String url);
    boolean isAuthenticated();
    String authErrorMessage();

    void setEventListener(IListener listener);
    void removeEventListener(IListener listener);


    boolean isReplicationComplete();
    String replicationErrorMessage();

    boolean hasProblem();

    int countRacks();
    List<IServer> getProblemServers();
    Iterable<IRack> rackIterable();
}