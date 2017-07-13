package org.nikbird.innopolis.datacentermonitor;

import android.content.Context;

import org.nikbird.innopolis.datacentermonitor.abstractclasses.AbstractDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;

import java.util.Objects;

/**
 * Created by nikbird on 12.07.17.
 */

public class LocalDataCenter extends AbstractDataCenter {
    private static final LocalDataCenter INSTANCE = new LocalDataCenter();
    public static final LocalDataCenter getInstance() { return INSTANCE; }
    private LocalDataCenter() {}

    private static class Server implements IServer {

        private int mRackNumber;
        private int mNumber;
        private State mState;

        public Server(int rackNumber, int number, State state) {
            mRackNumber = rackNumber;
            mNumber = number;
            mState = state;
        }

        @Override
        public int hashCode() {
            int result = 37 * 3 + Objects.hashCode(mRackNumber);
            result = 37 * result + Objects.hashCode(mNumber);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof IServer))
                return false;
            IServer server = (IServer) obj;
            return mRackNumber == server.getRackNumber()
                    && mNumber == server.getNumber()
                    && mState == server.getState();
        }

        @Override
        public int getRackNumber() {
            return mRackNumber;
        }

        @Override
        public int getNumber() {
            return mNumber;
        }

        @Override
        public State getState() {
            return mState;
        }
    }

    @Override
    public boolean start(Context context) {
        if (super.start(context)) {
            setRackCount(4);
            setRackCapacity(4);
            for(int rackNum = 1, rackCount = getRackCount(); rackNum <= rackCount; rackNum++) {
                for(int num = 1, maxNum = getRackCapacity(); num <= maxNum; num++)
                addServer(new Server(rackNum, num, IServer.State.GOOD));
            }
            postNotification(new Runnable() {
                @Override
                public void run() {
                    notifyReplicationComplete();
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void repairServer(IServer server) {

    }

}
