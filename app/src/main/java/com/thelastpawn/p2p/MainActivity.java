package com.thelastpawn.p2p;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NsdHelper mNSDHelper;
    private TextView mStatusView;
    private Handler mUpdateHandler;
    private ChatConnection mConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatusView = (TextView) findViewById(R.id.status);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };

        mConnection = new ChatConnection(mUpdateHandler);

        mNSDHelper = new NsdHelper(this);
        mNSDHelper.initListeners();
    }

    public void addChatLine(String line) {
        mStatusView.append("\n" + line);
    }

    public void clickRegister(View view) {
        // Register service
        if(mNSDHelper.isRegistered()){
            mNSDHelper.tearDown();
            return;
        }
        if (mConnection.getLocalPort() > -1) {
            mNSDHelper.registerService(mConnection.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }

    public void clickDiscover(View view) {
        if (mNSDHelper.isDiscovering())
            mNSDHelper.stopDiscovery();
        else
            mNSDHelper.discoverServices();
    }

    public void clickConnect(View v) {


        NsdServiceInfo service = mNSDHelper.getChosenServiceInfo();
        if (service != null) {
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }

    public void clickSend(View v) {
        EditText messageView = (EditText) this.findViewById(R.id.chatInput);
        if (messageView != null) {
            final String messageString = messageView.getText().toString();
            if (!messageString.isEmpty()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mConnection.sendMessage(messageString);
                    }
                }).start();
            }
            messageView.setText("");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNSDHelper.stopDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNSDHelper.discoverServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConnection.tearDown();
        mNSDHelper.tearDown();
    }

}