package com.studio.system.chattest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.nkzawa.emitter.Emitter;

/**
 * Created by N1cK0 on 15/03/17.
 */
public class BootRecieve extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, MessageService.class);
        //service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(service);
    }

}
