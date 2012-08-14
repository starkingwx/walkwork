package com.ivyinfo.activities;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ivyinfo.adapters.GroupSettingListAdapter;
import com.ivyinfo.contact.ContactManager;
import com.ivyinfo.contact.ContactManagerFactory;
import com.ivyinfo.contact.beans.Contact;
import com.ivyinfo.contact.beans.Group;

public class GroupSettingActivity extends Activity {
	private ContactManager cm;
	private Contact currentContact;
	private List<Group> groups;
	private GroupSettingListAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_setting_view);

		cm = ContactManagerFactory.getContactManager();

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		Contact contact = (Contact) bundle.get("contact");

		TextView nameTV = (TextView) findViewById(R.id.group_set_contact_name);
		nameTV.setText(contact.getDisplayName());

		currentContact = cm.getContactGroups(contact);

//		Log.d("walkwork", currentContact.toJSONObject().toString());

		adapter = new GroupSettingListAdapter(this, currentContact, cm);

		ListView groupList = (ListView) findViewById(R.id.group_set_grouplist);
		groupList.setAdapter(adapter);
		groupList.setOnItemClickListener(onGroupClickListener);

		refreshGroups();
	}

	private void refreshGroups() {
		groups = cm.getAllGroups();
		TextView infoTV = (TextView) findViewById(R.id.group_set_no_group_info_text);
		if (groups.size() == 0) {
			infoTV.setVisibility(View.VISIBLE);
		} else {
			infoTV.setVisibility(View.GONE);
		}
		adapter.setGroups(groups);
	}

	/**
	 * on back button clicked
	 * 
	 * @param view
	 */
	public void back(View view) {
		finish();
	}
	
	private OnItemClickListener onGroupClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final Group group = (Group) adapter.getItem(position);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					GroupSettingActivity.this)
					.setMessage(
							getString(R.string.delete)
									+ " "
									+ group.getTitle()
									+ " ?")
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									cm.deleteGroup(group.getId());
									groups.remove(group);
									adapter.notifyDataSetChanged();
								}
							})
					.setNegativeButton(R.string.cancel,
							null);
			builder.show();
		}
	};

	/**
	 * create new group on create button clicked
	 * 
	 * @param view
	 */
	public void createNewGroup(View view) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View newGroupDlgView = factory.inflate(
				R.layout.dlg_create_new_group, null);
		final EditText titleET = (EditText) newGroupDlgView
				.findViewById(R.id.new_group_name_et);

		new AlertDialog.Builder(GroupSettingActivity.this)
				.setTitle(R.string.pls_input)
				.setView(newGroupDlgView)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String title = titleET.getEditableText()
										.toString().trim();
								if (title.length() > 0) {
									cm.addGroup(title);
									refreshGroups();
								}
							}
						}).setNegativeButton(R.string.cancel, null).show();

	}
}
