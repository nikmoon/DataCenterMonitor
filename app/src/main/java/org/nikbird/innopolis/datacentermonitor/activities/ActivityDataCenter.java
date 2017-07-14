package org.nikbird.innopolis.datacentermonitor.activities;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.nikbird.innopolis.datacentermonitor.LocalDataCenter;
import org.nikbird.innopolis.datacentermonitor.R;
import org.nikbird.innopolis.datacentermonitor.abstractclasses.ActivityListener;
import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityDataCenter extends ActivityListener implements View.OnClickListener {

    ConstraintLayout mDataCenterLayout;
    private Map<IServer, View> mServerViews;

    private List<ViewGroup> mRackViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onServerStateChanged(IServer server, IServer.State statePrev) {
        ImageButton serverView = (ImageButton) mServerViews.get(server);
        switch (server.getState()) {
            case FAIL:
                serverView.setImageResource(R.drawable.server_fail_120);
                break;
            case GOOD:
                serverView.setImageResource(R.drawable.server_ok_120);
                break;
        }
    }

    private ImageButton newButton(int imageResId) {
        ImageButton button = new ImageButton(this);

        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button.setAdjustViewBounds(true);

        button.setImageResource(imageResId);
        button.setId(View.generateViewId());
        return button;
    }

    public ConstraintLayout createRackView(int rackId) {
        ConstraintLayout rackView = new ConstraintLayout(this);
        rackView.setBackgroundResource(R.color.colorRack);
        rackView.setId(rackId);
        return rackView;
    }

    public void addNewRackView2Rows() {
        int rackId = View.generateViewId();
        ConstraintLayout rackView = createRackView(rackId);
        mRackViews.add(rackView);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (mRackViews.size() == 1) {
            params.topToTop = mDataCenterLayout.getId();
            params.leftToLeft = mDataCenterLayout.getId();
            params.topMargin = 20;
            params.leftMargin = 20;
        } else {
            int div2Rem = mRackViews.size() % 2;
            if (div2Rem == 0) { // стойка во второй колонке
                ViewGroup leftRackView = mRackViews.get(mRackViews.size() - 2);
                params.topToTop = leftRackView.getId();
                params.bottomToBottom = leftRackView.getId();
                params.leftToRight = leftRackView.getId();
                params.leftMargin = 20;
            } else { // стойка в первой колонке
                ViewGroup topRackView = mRackViews.get(mRackViews.size() - 3);
                params.topToBottom = topRackView.getId();
                params.leftToLeft = topRackView.getId();
                params.rightToRight = topRackView.getId();
                params.topMargin = 20;
                params.bottomMargin = 20;
            }
        }
        mDataCenterLayout.addView(rackView, params);
    }


    public ImageButton createServerView(IServer server) {
        ImageButton serverView;

        if (server.getState() == IServer.State.GOOD)
            serverView = newButton(R.drawable.server_ok_120);
        else
            serverView = newButton(R.drawable.server_fail_120);

        serverView.setOnClickListener(this);
        return serverView;
    }

    public void insertServersInRack(ViewGroup rackView, IServer[] servers) {
        View topServerView = null;
        IServer server;

        for(int serverNum = 1, maxCount = servers.length; serverNum <= maxCount; serverNum++) {
            server = servers[serverNum - 1];
            if (server == null)
                continue;
            ImageButton serverView = (ImageButton) mServerViews.get(server);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (topServerView == null) {
                params.topToTop = rackView.getId();
                params.leftToLeft = rackView.getId();
            } else {
                params.topToBottom = topServerView.getId();
                params.leftToLeft = topServerView.getId();
            }
            rackView.addView(serverView, params);
            topServerView = serverView;
        }
    }

    @Override
    public void onReplicationComplete(IDataCenter dataCenter) {
        setContentView(R.layout.activity_data_center);
        mServerViews = new HashMap<>();
        mRackViews = new ArrayList<>();

        mDataCenterLayout = (ConstraintLayout) findViewById(R.id.ltDataCenterRoom);
        int rackCount = mDataCenter.getRackCount();
        int rackCapacity = mDataCenter.getRackCapacity();

        // создаем вьюшки для стоек
        List<IServer[]> racks = new ArrayList<>(rackCount);
        for(int rackNum = 1; rackNum <= rackCount; rackNum++) {
            addNewRackView2Rows();
            racks.add(new IServer[rackCapacity]);
        }

        // создаем вьюшки для серверов в стойках
        for(IServer server: mDataCenter) {
            int rackNum = server.getRackNumber();
            int serverNum = server.getNumber();
            racks.get(rackNum - 1)[serverNum - 1] = server;
            ImageButton serverView = createServerView(server);
            mServerViews.put(server, serverView);
        }

        // добавляем сервера в стойки
        for(int rackNum = 1; rackNum <= rackCount; rackNum++) {
            insertServersInRack(mRackViews.get(rackNum - 1), racks.get(rackNum - 1));
        }
        mDataCenter.setEventListener(this);
    }

    public void onClick(View view) {
        for(Map.Entry<IServer, View> entry: mServerViews.entrySet()) {
            if (entry.getValue() == view) {
                IServer server = entry.getKey();
                ((LocalDataCenter)mDataCenter).testServerStateChanged(server);
                break;
            }
        }
    }
}
