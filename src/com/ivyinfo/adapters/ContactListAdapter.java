package com.ivyinfo.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivyinfo.activities.R;
import com.ivyinfo.contact.beans.Contact;

/**
 * ContactListAdapter
 * 
 * @author sk
 * 
 */
public class ContactListAdapter extends BaseAdapter {
	private List<Contact> contacts;
	private Context context;

	public ContactListAdapter(Context context) {
		contacts = new ArrayList<Contact>();
		this.context = context;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
		notifyDataSetChanged();
	}

	public List<Contact> getContacts() {
		return contacts;
	}

	@Override
	public int getCount() {
		return contacts.size();
	}

	@Override
	public Object getItem(int position) {
		return contacts.get(position);
	}

	@Override
	public long getItemId(int position) {
		Contact c = contacts.get(position);
		if (c == null) {
			return -1;
		}
		return c.getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Contact contact = contacts.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.template_contact_list_item, null);
			viewHolder.contactNameView = (TextView) convertView
					.findViewById(R.id.contact_li_name);
			viewHolder.timesContactedView = (TextView) convertView
					.findViewById(R.id.contact_li_contacted_times);
			viewHolder.phonesView = (LinearLayout) convertView
					.findViewById(R.id.contact_li_phones);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
	
		viewHolder.contactNameView.setText(contact.getDisplayName());
		int times = contact.getTimesContacted();
		if (times > 0) {
			viewHolder.timesContactedView.setText("(" + times + ")");
		} else {
			viewHolder.timesContactedView.setText("");
		}
		List<String> phones = contact.getPhones();
		viewHolder.phonesView.removeAllViews();
		if (phones != null) {
			for (String phone : phones) {
				TextView numberTV = new TextView(context);
				numberTV.setText(phone);
				viewHolder.phonesView.addView(numberTV);
			}
		}
		
		return convertView;
	}

	final class ViewHolder {
		public TextView contactNameView;
		public TextView timesContactedView;
		public LinearLayout phonesView;
	}
}
