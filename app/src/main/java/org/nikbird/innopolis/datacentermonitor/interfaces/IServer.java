package org.nikbird.innopolis.datacentermonitor.interfaces;

/**
 * Created by nikbird on 11.07.17.
 */
public interface IServer {

    enum State {
        GOOD,
        FAIL
    }

    String toString();
    int getRackNumber();
    int getNumber();
    State getState();
}
