package com.ivyinfo.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ivyinfo.activities.R;
import com.ivyinfo.contact.beans.Group;

public class GroupListAdapter extends BaseAdapter {
	private Context context;
	private List<Group> groups;

	public GroupListAdapter(Context context) {
		this.context = context;
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
		Group group = groups.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.template_group_list_item, null);
			viewHolder.groupTitleTextView = (TextView) convertView
					.findViewById(R.id.group_li_title_textview);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		StringBuffer sb = new StringBuffer();
		sb.append(group.getTitle()).append("(").append(group.getSummaryCount()).append(")");
		viewHolder.groupTitleTextView.setText(sb.toString());

		return convertView;

	}

	final class ViewHolder {
		public TextView groupTitleTextView;
	}

}
