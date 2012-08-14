package com.ivyinfo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ivyinfo.services.ContactSyncService;

/**
 * Broadcaster receiver for auto start on boot
 * 
 * @author sk
 * 
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent startIntent = new Intent(context, ContactSyncService.class);
			startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(startIntent);
		}
	}

}
