package com.ivyinfo.services;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import com.ivyinfo.contact.ContactManager;
import com.ivyinfo.contact.ContactManagerFactory;

public class ContactSyncService extends Service {
	private ServiceHandler mServiceHandler;
	
	private ContentObserver mObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// 当联系人表发生变化时进行相应的操作
			Log.d("walkwork", "Contacts Modified");

			ContactManager cm = ContactManagerFactory.getContactManager();
			cm.setIsModifyFlag(true);
			cm.getAllContactsByCompoundSort();
		}
	};

	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceHandler = new ServiceHandler(thread.getLooper());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// If we get killed, after returning from here, restart
		Log.d("walkwork", "ContactSyncService started");
		
		mServiceHandler.sendEmptyMessage(0);

		getContentResolver().registerContentObserver(
				ContactsContract.Contacts.CONTENT_URI, true, mObserver);
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(mObserver);
	}
	
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				Log.d("walkwork", "init contact manager");
				ContactManager cm = ContactManagerFactory.getContactManager();
				if (cm == null) {
					ContactManagerFactory
							.initContactManager(ContactSyncService.this);
					cm = ContactManagerFactory.getContactManager();
				}
				cm.getAllContactsByCompoundSort();
			}
		}
	}

}
