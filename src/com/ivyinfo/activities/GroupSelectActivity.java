package com.ivyinfo.activities;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ivyinfo.adapters.GroupListAdapter;
import com.ivyinfo.contact.ContactManager;
import com.ivyinfo.contact.ContactManagerFactory;
import com.ivyinfo.contact.beans.Group;

public class GroupSelectActivity extends Activity {
	private ContactManager cm;
	private GroupListAdapter groupsAdatper;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_selection_view);
		cm = ContactManagerFactory.getContactManager();

		List<Group> groups = cm.getAllGroups();
		
		// add all contacts group
		Group allContactsGroup = new Group();
		allContactsGroup.setTitle(getString(R.string.all_contacts));
		int allCount = getIntent().getIntExtra("all_contacts_count", 0);
		allContactsGroup.setSummaryCount(allCount);
		groups.add(0, allContactsGroup);
		
		ListView groupList = (ListView) findViewById(R.id.group_list);
		groupsAdatper = new GroupListAdapter(this);
		groupList.setAdapter(groupsAdatper);
		groupList.setOnItemClickListener(groupListOnItemClickListener);

		
		groupsAdatper.setGroups(groups);

	}

	private OnItemClickListener groupListOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			Group group = (Group) groupsAdatper.getItem(position);
			Intent intent = GroupSelectActivity.this.getIntent();
			Bundle bundle = new Bundle();
			bundle.putInt("group_id", group.getId());
			bundle.putString("group_title", group.getTitle());
			intent.putExtras(bundle);
			setResult(WalkworkMainActivity.RESULT_OK, intent);
			GroupSelectActivity.this.finish();
			GroupSelectActivity.this.overridePendingTransition(
					0, R.anim.slide_down);
		}
	};

	/**
	 * action for cancel button
	 * @param view
	 */
	public void onCancel(View view) {
		this.finish();
		overridePendingTransition(0, R.anim.slide_down);
	}
}
