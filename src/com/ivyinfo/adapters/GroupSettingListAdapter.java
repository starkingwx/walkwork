package com.ivyinfo.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ivyinfo.activities.R;
import com.ivyinfo.contact.ContactManager;
import com.ivyinfo.contact.beans.Contact;
import com.ivyinfo.contact.beans.Group;

public class GroupSettingListAdapter extends BaseAdapter {
	private Activity context;
	private Contact contact;
	private List<Group> groups;
	private ContactManager cm;

	public GroupSettingListAdapter(Activity context, Contact contact,
			ContactManager cm) {
		this.context = context;
		this.contact = contact;
		this.cm = cm;
		groups = new ArrayList<Group>();

	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
		notifyDataSetChanged();
	}

	public List<Group> getGroups() {
		return groups;
	}

	@Override
	public int getCount() {
		return groups.size();
	}

	@Override
	public Object getItem(int position) {
		return groups.get(position);
	}

	@Override
	public long getItemId(int position) {
		Group g = groups.get(position);
		if (g == null) {
			return -1;
		}
		return g.getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		final Group group = groups.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.template_group_setting_list_item, null);
			viewHolder.groupTitleTextView = (TextView) convertView
					.findViewById(R.id.group_setting_title);
			viewHolder.selectionCB = (CheckBox) convertView
					.findViewById(R.id.group_setting_select_cb);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.groupTitleTextView.setText(group.getTitle());
		viewHolder.selectionCB.setChecked(false);

		// show which group the contact belongs to
		List<Group> groupMems = contact.getGroups();
		if (groupMems != null) {
			for (Group g : groupMems) {
				if (g.getId() == group.getId()) {
					viewHolder.selectionCB.setChecked(true);
					break;
				}
			}
		}

		viewHolder.selectionCB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				Log.d("walkwork", "setOnClickListener: " + group.getTitle()
						+ " check: " + cb.isChecked());

				if (cb.isChecked()) {
					List<Group> groupMems = contact.getGroups();
					boolean isExist = false;
					if (groupMems != null) {
						for (Group g : groupMems) {
							if (g.getId() == group.getId()) {
								isExist = true;
								break;
							}
						}
					}
					if (!isExist) {
						contact.addGroup(group);
					}
				} else {
					// remove contact from selected group
					List<Group> groupMems = contact.getGroups();
					if (groupMems != null) {
						for (Group g : groupMems) {
							if (g.getId() == group.getId()) {
								groupMems.remove(g);
								break;
							}
						}
					}
				}

				// save the contact group membership
				cm.saveContactGroups(contact);
			}
		});

		return convertView;

	}

	final class ViewHolder {
		public TextView groupTitleTextView;
		public CheckBox selectionCB;
	}
}
