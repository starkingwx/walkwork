package com.ivyinfo.activities;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivyinfo.contact.beans.Contact;

public class ContactOperationMenuActivity extends Activity {
	private Contact contact;
	private TextView title;
	private LinearLayout menuBody;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_operation_menu_view);

		Bundle bundle = getIntent().getExtras();
		contact = (Contact) bundle.get("contact");

		title = (TextView) findViewById(R.id.contact_op_menu_title);
		menuBody = (LinearLayout) findViewById(R.id.contact_op_menu_body);

		title.setText(getString(R.string.select_operation) + "("
				+ contact.getDisplayName() + ")");
	}

	/**
	 * action for cancel button
	 * 
	 * @param view
	 */
	public void onCancel(View view) {
		this.finish();
		overridePendingTransition(0, R.anim.slide_down);
	}

	/**
	 * action for dial button
	 * 
	 * @param view
	 */
	public void onDial(View view) {
		List<String> phones = contact.getPhones();
		if (phones == null || phones.size() <= 0) {
			onNoNumber(contact.getDisplayName());
		} else if (phones.size() == 1) {
			String phone = phones.get(0);
			doActualDial(phone);
			onCancel(view);
		} else {
			title.setText(getString(R.string.select_dial_number) + " ("
					+ contact.getDisplayName() + ")");
			LinearLayout next = (LinearLayout) findViewById(R.id.contact_op_menu_next);
			Button bt1 = (Button) findViewById(R.id.contact_op_menu_next_button1);
			String phone1 = phones.get(0);
			bt1.setText(phone1);
			bt1.setTag(phone1);
			bt1.setOnClickListener(dialNumberBtCL);
			next.removeAllViews();
			next.addView(bt1);
			for (int i = 1; i < phones.size(); i++) {
				Button bt = new Button(this);
				String phone = phones.get(i);
				bt.setText(phone);
				bt.setTextSize(20);
				bt.setTextColor(bt1.getTextColors());
				bt.setTag(phone);
				bt.setOnClickListener(dialNumberBtCL);
				next.addView(bt);
			}
			menuBody.setVisibility(View.GONE);
			next.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * action for SMS button
	 * 
	 * @param view
	 */
	public void onSMS(View view) {
		List<String> phones = contact.getPhones();
		if (phones == null || phones.size() <= 0) {
			onNoNumber(contact.getDisplayName());
		} else if (phones.size() == 1) {
			String phone = phones.get(0);
			doActualSMS(phone);
			onCancel(view);
		} else {
			title.setText(getString(R.string.select_sms_number) + " ("
					+ contact.getDisplayName() + ")");
			LinearLayout next = (LinearLayout) findViewById(R.id.contact_op_menu_next);
			Button bt1 = (Button) findViewById(R.id.contact_op_menu_next_button1);
			String phone1 = phones.get(0);
			bt1.setText(phone1);
			bt1.setTag(phone1);
			bt1.setOnClickListener(smsNumberBtCL);
			next.removeAllViews();
			next.addView(bt1);
			for (int i = 1; i < phones.size(); i++) {
				Button bt = new Button(this);
				String phone = phones.get(i);
				bt.setText(phone);
				bt.setTextSize(20);
				bt.setTextColor(bt1.getTextColors());
				bt.setTag(phone);
				bt.setOnClickListener(smsNumberBtCL);
				next.addView(bt);
			}
			menuBody.setVisibility(View.GONE);
			next.setVisibility(View.VISIBLE);
		}
	}

	private void onNoNumber(String name) {
		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		bundle.putString("name", name);
		intent.putExtras(bundle);
		setResult(WalkworkMainActivity.RESULT_NO_NUMBER, intent);
		finish();
		overridePendingTransition(0, R.anim.slide_down);
	}

	/**
	 * do actual dial operation
	 * 
	 * @param phone
	 *            - phone number
	 */
	private void doActualDial(String phone) {
		Uri uri = Uri.parse("tel:" + phone);
		Intent intent = new Intent(Intent.ACTION_CALL, uri);
		startActivity(intent);
		
	}

	/**
	 * send short message actually
	 * 
	 * @param phone
	 */
	private void doActualSMS(String phone) {
		Uri uri = Uri.parse("smsto:" + phone);
		Intent it = new Intent(Intent.ACTION_SENDTO);
		it.setData(uri);
		startActivity(it);
	}
	
	private OnClickListener dialNumberBtCL = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String phone = (String) v.getTag();
			doActualDial(phone);
			onCancel(v);
		}
	};
	
	private OnClickListener smsNumberBtCL = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String phone = (String)v.getTag();
			doActualSMS(phone);
			onCancel(v);
		}
	};
}
