package com.thelastpawn.p2p;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * Created by pawan on 31/10/17.
 */

public class P2P_Helper {

    private final Handler sendingHandler;
    private final Handler recievingHandler;
    private final Context mContext;

    P2P_Helper(Handler handler, Context context) {
        mContext = context;
        sendingHandler = handler;
        recievingHandler = new Handler() {
            @Override
            public void handleMessage(Message msg){

            }
        };
    }

    public Handler getRecievingHandler() {
        return recievingHandler;
    }

}
