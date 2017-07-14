package org.nikbird.innopolis.datacentermonitor;

import android.content.Context;
import android.content.Intent;

import org.nikbird.innopolis.datacentermonitor.abstractclasses.AbstractDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;
import org.nikbird.innopolis.datacentermonitor.services.ServiceDataCenterListener;

import java.util.Date;
import java.util.Objects;
import java.util.Random;

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
                    && mNumber == server.getNumber();
//                    && mState == server.getState();
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

        public State setState(State newState) {
            State state = mState;
            mState = newState;
            return state;
        }
    }

    @Override
    public void deliveryDataCenterInfo(int rackCount, int rackCapacity, int[] rackNums, int[] serverNums, IServer.State[] states) {
        setRackCount(rackCount);
        setRackCapacity(rackCapacity);
        for(int index = 0; index < rackNums.length; index++)
            addServer(new Server(rackNums[index], serverNums[index], states[index]));
        notifyReplicationComplete();
    }

    private Intent makeServiceIntent(ServiceTask task) {
        Intent intent = new Intent(getContext(), ServiceDataCenterListener.class);
        intent.putExtra(SERVICE_TASK_TAG, task.toString());
        return intent;
    }

    @Override
    public boolean start(Context context) {
        if (super.start(context)) {
            context.startService(makeServiceIntent(ServiceTask.DELIVERY_STRUCTURE));
        }
        return false;
    }

    @Override
    public void repairServer(IServer server) {

    }


    public void testRandomServerBreaker(final int delayMillis) {
        Random random = new Random(System.currentTimeMillis());
        int serverIndex = random.nextInt(getServers().size());
        IServer server = null;
        int counter = 0;
        for(IServer server1: getServers()) {
            if (serverIndex == counter)
                server = server1;
            counter++;
        }

        if (server != null && server.getState() != IServer.State.FAIL) {
            final IServer.State state = server.getState();
            final Server serverObj = (Server)server;
            serverObj.setState(IServer.State.FAIL);
            postNotification(new Runnable() {
                @Override
                public void run() {
                    notifyServerStateChanged(serverObj, state);
                    testRandomServerBreaker(delayMillis);
                }
            }, delayMillis);
        }
        else {
            postNotification(new Runnable() {
                @Override
                public void run() {
                    testRandomServerBreaker(delayMillis);
                }
            }, delayMillis);
        }
    }



    public void testServerStateChanged(IServer server) {
        final IServer.State state = server.getState();
        final Server serverObj = (Server) server;
        switch (state) {
            case FAIL:
                serverObj.setState(IServer.State.GOOD);
                break;
            case GOOD:
                serverObj.setState(IServer.State.FAIL);
                break;
        }

        postNotification(new Runnable() {
            @Override
            public void run() {
                notifyServerStateChanged(serverObj, state);
            }
        });
    }

}
