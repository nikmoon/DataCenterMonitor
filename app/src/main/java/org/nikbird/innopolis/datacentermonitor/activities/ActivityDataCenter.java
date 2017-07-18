package org.nikbird.innopolis.datacentermonitor.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.nikbird.innopolis.datacentermonitor.R;
import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IRack;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;
import org.nikbird.innopolis.datacentermonitor.services.ServiceDataCenter;

import java.util.HashMap;
import java.util.Map;

public class ActivityDataCenter extends AppCompatActivity implements IDataCenter.IListener, View.OnClickListener {

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ServiceDataCenter.LocalBinder binder = (ServiceDataCenter.LocalBinder) iBinder;
            mDataCenter = binder.getDataCenter();
            mDataCenter.setEventListener(ActivityDataCenter.this);
            onReplicationEvent();
        }

        @Override public void onServiceDisconnected(ComponentName componentName) {}
    };

    private IDataCenter mDataCenter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_center);

        bindService(new Intent(getApplicationContext(), ServiceDataCenter.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (mDataCenter != null) {
            mDataCenter.removeEventListener(this);
            mDataCenter.resetAuthentication();
            unbindService(mConnection);
        }
    }

    @Override public void onAuthenticationEvent() {}
    @Override public void onServerAdded(IServer server) {}
    @Override public void onServerRemoved(IServer server, IServer.ServerPosition prevPosition) {}

    @Override public void onServerStateChanged(IServer server, IServer.State prevState) {
        if (server.state() == prevState)
            return;
        try {
            RackView rackView = mRackViews.get(server.position().rack());
            ImageButton serverView = (ImageButton) rackView.serverViews()[server.position().index()];
            switch (server.state()) {
                case FAIL:
                    serverView.setImageResource(R.drawable.server_fail_120);
                    break;
                case GOOD:
                    serverView.setImageResource(R.drawable.server_ok_120);
                    break;
            }
        } catch (IndexOutOfBoundsException e) {

        }
    }

    private class RackView {
        private ViewGroup mViewGroup;
        private View[] mServerViews;

        public RackView(final ViewGroup viewGroup, final View[] serverViews) {
            mViewGroup = viewGroup;
            mServerViews = serverViews;
        }
        public ViewGroup viewGroup() { return mViewGroup; }
        public View[] serverViews() { return mServerViews; }
    }

    private Map<IRack, RackView> mRackViews;
    private Map<Integer, IServer> mServers;

    @Override public void onReplicationEvent() {
        if (!mDataCenter.isReplicationComplete()) {
            String errorMsg = mDataCenter.replicationErrorMessage();
            if (errorMsg != null) {
                Toast.makeText(this, mDataCenter.replicationErrorMessage(), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        GridLayout serverRoomLayout = (GridLayout) findViewById(R.id.ltServerRoomGrid);
        mRackViews = new HashMap<>();
        mServers = new HashMap<>();
        int index = 0;
        for (IRack rack : mDataCenter.rackIterable()) {
            RackView rackView;
            if (rack == null) {
                rackView = new RackView(new ConstraintLayout(this), null);
                rackView.viewGroup().setVisibility(View.INVISIBLE);
            } else {
                rackView = createRackView(rack);
                mRackViews.put(rack, rackView);
            }
            serverRoomLayout.addView(rackView.viewGroup(), index, rackParams(rack, serverRoomLayout, rackView));
            index++;
        }
    }

    private GridLayout.LayoutParams rackParams(IRack rack, GridLayout serverRoomLayout, RackView rackView) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.setMargins(16,16,16,16);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        return params;
    }

    private RackView createRackView(IRack rack) {
        ConstraintLayout viewGroup = new ConstraintLayout(this);
        viewGroup.setBackgroundResource(R.color.colorRack);
        viewGroup.setId(View.generateViewId());
        View[] serverViews = new View[rack.capacity()];

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = viewGroup.getId();
        params.leftToLeft = viewGroup.getId();
        params.rightToRight = viewGroup.getId();
        TextView tvRackNumber = new TextView(this);
        tvRackNumber.setId(View.generateViewId());
        tvRackNumber.setText("Стойка № " + (rack.position().rackIndex() + 1));
        viewGroup.addView(tvRackNumber, params);

        int index = 0;
        for (IServer server : rack) {
            ImageButton serverView = new ImageButton(this);
            serverView.setOnClickListener(this);
            serverView.setId(View.generateViewId());
            int resId = R.drawable.server_empty_120;
            if (server != null)
                switch (server.state()) {
                    case GOOD:
                        resId = R.drawable.server_ok_120;
                        break;
                    case FAIL:
                        resId = R.drawable.server_fail_120;
                        break;
                    default:
                        break;
                }
            serverView.setImageResource(resId);
            mServers.put(serverView.getId(), server);
            params = new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftToLeft = viewGroup.getId();
            params.rightToRight = viewGroup.getId();
            if (index == 0)
                params.topToBottom = tvRackNumber.getId();
            else
                params.topToBottom = serverViews[index - 1].getId();
            viewGroup.addView(serverView, params);
            serverViews[index] = serverView;
            index++;
        }
        return new RackView(viewGroup, serverViews);
    }

    @Override public void onClick(View view) {
        ImageButton imageButton = (ImageButton) view;
        IServer server = mServers.get(view.getId());
        String msg;
        if (server != null) {
            msg = "Стойка: " + server.position().rack().position().rackIndex()
                    + "\nСервер: " + server.position().index();
        } else {
            msg = "Здесь нет сервера";
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
