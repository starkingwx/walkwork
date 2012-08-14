package com.ivyinfo.activities;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivyinfo.adapters.ContactListAdapter;
import com.ivyinfo.constant.KeyboardType;
import com.ivyinfo.constant.SelfDefineKeyCode;
import com.ivyinfo.contact.ContactManager;
import com.ivyinfo.contact.ContactManagerFactory;
import com.ivyinfo.contact.beans.Contact;
import com.ivyinfo.services.ContactSyncService;
import com.ivyinfo.util.DisplayUtil;

public class WalkworkMainActivity extends Activity {
	final public static int REQ_GROUP_SELECTION = 100;
	final public static int REQ_CONTACT_OP = 101;
	final public static int REQ_GROUPING = 102;
	final public static int RESULT_OK = 200;
	final public static int RESULT_NO_NUMBER = 201;
	private ContactManager cm;
	private ContactListAdapter contactListAdpater;

	private List<Contact> allContacts = null; // all contacts

	private List<Contact> searchSourceContacts = null; // source contacts for
														// searching, and it's also current contact list

	private TextView searchField;
	private KeyboardView keyboardView;
	private boolean isKeyboardShown = false;

	private KeyboardType currentKeyboardType = KeyboardType.qwert_pad;
	private Keyboard numberKeyboard;
	private Keyboard qwertKeyboard;

	private int currentGroupID = -1;

	private boolean isPopupMenuOpenFlag = false;
	private PopupWindow popupMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		cm = ContactManagerFactory.getContactManager();
		if (cm == null) {
			ContactManagerFactory.initContactManager(this);
			cm = ContactManagerFactory.getContactManager();
		}

		Intent intent = new Intent(this, ContactSyncService.class);
		startService(intent);

		initSysPopupMenu();
		initInputPad();
		initContactList();
	}

	private void initSysPopupMenu() {
		LayoutInflater layoutInflater = getLayoutInflater();
		View popupMenuView = layoutInflater.inflate(R.layout.sys_popup_menu,
				null);
		int popW = DisplayUtil.dip2px(this, 100);
		int popH = DisplayUtil.dip2px(this, 86);
		popupMenu = new PopupWindow(popupMenuView, popW, popH);

	}

	/**
	 * on sys popup menu button click
	 * 
	 * @param view
	 */
	public void onSysPopupMenuButtonClick(View view) {
		if (!isPopupMenuOpenFlag) {
			openSysPopupMenu();
		} else {
			closeSysPopupMenu();
		}
	}

	private void openSysPopupMenu() {
		if (!isPopupMenuOpenFlag) {
			isPopupMenuOpenFlag = true;
			popupMenu.showAsDropDown(findViewById(R.id.sys_menu_bt), 0, 0);
		}
	}

	private void closeSysPopupMenu() {
		if (isPopupMenuOpenFlag) {
			isPopupMenuOpenFlag = false;
			popupMenu.dismiss();
		}
	}

	/**
	 * on add new contact menu item click
	 * 
	 * @param view
	 */
	public void onNewContact(View view) {
		closeSysPopupMenu();
		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
		startActivity(intent);
	}

	/**
	 * on add new group menu item click
	 * 
	 * @param view
	 */
	public void onNewGroup(View view) {
		closeSysPopupMenu();
		LayoutInflater factory = LayoutInflater.from(this);
		final View newGroupDlgView = factory.inflate(
				R.layout.dlg_create_new_group, null);
		final EditText titleET = (EditText) newGroupDlgView
				.findViewById(R.id.new_group_name_et);

		new AlertDialog.Builder(WalkworkMainActivity.this)
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
								}
							}
						}).setNegativeButton(R.string.cancel, null).show();
	}

	private void initContactList() {
		final ListView contactListView = (ListView) findViewById(R.id.contact_list);
		contactListAdpater = new ContactListAdapter(this);
		contactListView.setAdapter(contactListAdpater);
		contactListView.setOnItemClickListener(contactListOnItemCL);
		contactListView.setOnItemLongClickListener(contactListOnItemLongCL);
		contactListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (isKeyboardShown) {
					hideKeyboard();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		allContacts = cm.getAllContactsByCompoundSort();
		searchSourceContacts = allContacts;
		contactListAdpater.setContacts(allContacts);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshContactsInCurrentGroup();
	}

	private void initInputPad() {
		searchField = (TextView) findViewById(R.id.name_search_field);
		// searchField.setInputType(InputType.TYPE_NULL);
		searchField.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				searchContacts(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		numberKeyboard = new Keyboard(this, R.xml.phonepad);
		qwertKeyboard = new Keyboard(this, R.xml.qwerty);

		keyboardView = (KeyboardView) findViewById(R.id.keyboard_view);
		switchKeyBoard(KeyboardType.qwert_pad);
		keyboardView.setEnabled(true);
		keyboardView.setPreviewEnabled(false);

		ImageButton backspaceBt = (ImageButton) findViewById(R.id.backspace_bt);
		backspaceBt.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				searchField.setText("");
				return false;
			}
		});
	}

	private void switchKeyBoard(KeyboardType type) {
		currentKeyboardType = type;
		if (type == KeyboardType.number_pad) {
			keyboardView.setKeyboard(numberKeyboard);
			keyboardView.setOnKeyboardActionListener(numberPadAL);
		} else if (type == KeyboardType.qwert_pad) {
			keyboardView.setKeyboard(qwertKeyboard);
			keyboardView.setOnKeyboardActionListener(qwertPadAL);
		}
		searchField.setText("");
		searchContacts("");
	}

	/**
	 * number pad on keyboard action listener
	 */
	private OnKeyboardActionListener numberPadAL = new OnKeyboardActionListener() {

		@Override
		public void swipeUp() {
		}

		@Override
		public void swipeRight() {
		}

		@Override
		public void swipeLeft() {
		}

		@Override
		public void swipeDown() {
		}

		@Override
		public void onText(CharSequence text) {
		}

		@Override
		public void onRelease(int primaryCode) {
		}

		@Override
		public void onPress(int primaryCode) {
		}

		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			Editable editable = searchField.getEditableText();
			if (primaryCode == SelfDefineKeyCode.KEYCODE_SWITCH_ABC) {
				// switch to abc keyboard
				switchKeyBoard(KeyboardType.qwert_pad);
			} else if (primaryCode == SelfDefineKeyCode.KEYCODE_ADD_TO_CONTACT) {
				// add to contacts
				Intent intent = new Intent(Intent.ACTION_INSERT);
				intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

				// See android.provider.ContactsContract.Intents.Insert for the
				// complete list.
				intent.putExtra(ContactsContract.Intents.Insert.PHONE,
						editable.toString());
				startActivity(intent);
			} else if (primaryCode == SelfDefineKeyCode.KEYCODE_DIAL) {
				String number = editable.toString();
				if (number.length() >= 3) {
					doActualDial(number);
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							WalkworkMainActivity.this).setMessage(
							R.string.input_at_least_3_digits).setNeutralButton(
							R.string.re_input, null);
					builder.show();
				}
			} else if (primaryCode == SelfDefineKeyCode.KEYCODE_MSG) {
				String number = editable.toString();
				doActualSMS(number);
			} else {
				editable.append((char) primaryCode);
			}
		}
	};

	private OnKeyboardActionListener qwertPadAL = new OnKeyboardActionListener() {

		@Override
		public void swipeUp() {
		}

		@Override
		public void swipeRight() {
		}

		@Override
		public void swipeLeft() {
		}

		@Override
		public void swipeDown() {
		}

		@Override
		public void onText(CharSequence text) {
		}

		@Override
		public void onRelease(int primaryCode) {
		}

		@Override
		public void onPress(int primaryCode) {
		}

		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			Editable editable = searchField.getEditableText();
			if (primaryCode == SelfDefineKeyCode.KEYCODE_SWITCH_123) {
				// switch to 123 keyboard
				switchKeyBoard(KeyboardType.number_pad);
			} else {
				editable.append((char) primaryCode);
			}

		}
	};

	private OnItemClickListener contactListOnItemCL = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			closeSysPopupMenu();
			Contact contact = (Contact) contactListAdpater.getItem(position);
			if (contact != null) {
				Intent intent = new Intent(WalkworkMainActivity.this,
						ContactOperationMenuActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("contact", contact);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQ_CONTACT_OP);
				overridePendingTransition(R.anim.slide_up_in, 0);
			}
		}
	};

	private OnItemLongClickListener contactListOnItemLongCL = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			closeSysPopupMenu();
			final Contact contact = (Contact) contactListAdpater
					.getItem(position);
			new AlertDialog.Builder(WalkworkMainActivity.this)
					.setTitle(
							getString(R.string.select_operation) + " ("
									+ contact.getDisplayName() + ")")
					.setItems(R.array.contact_long_click_menu,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										viewContact(contact);
										break;
									case 1:
										editContact(contact);
										break;
									case 2:
										grouping(contact);
										break;
									case 3:
										deleteContact(contact);
										break;
									default:
										break;
									}
								}

								/**
								 * view contact detail info
								 * 
								 * @param c
								 *            - contact
								 */
								private void viewContact(Contact c) {
									Intent intent = new Intent(
											Intent.ACTION_VIEW);
									intent.setData(ContentUris.withAppendedId(
											Contacts.CONTENT_URI, c.getId()));
									startActivity(intent);
								}

								/**
								 * edit contact
								 * 
								 * @param c
								 */
								private void editContact(Contact c) {
									Intent intent = new Intent(
											Intent.ACTION_EDIT);
									intent.setData(ContentUris.withAppendedId(
											Contacts.CONTENT_URI, c.getId()));
									startActivity(intent);
								}

								/**
								 * start group setting activity
								 * 
								 * @param c
								 *            - contact
								 */
								private void grouping(Contact c) {
									Intent intent = new Intent(
											WalkworkMainActivity.this,
											GroupSettingActivity.class);
									Bundle bundle = new Bundle();
									bundle.putSerializable("contact", c);
									intent.putExtras(bundle);
									// startActivity(intent);
									startActivityForResult(intent, REQ_GROUPING);

								}

								/**
								 * delete contact
								 * 
								 * @param c
								 */
								private void deleteContact(final Contact c) {
									AlertDialog.Builder builder = new AlertDialog.Builder(
											WalkworkMainActivity.this)
											.setMessage(
													getString(R.string.delete)
															+ " "
															+ c.getDisplayName()
															+ " ?")
											.setPositiveButton(
													R.string.ok,
													new DialogInterface.OnClickListener() {

														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															cm.deleteContact(c
																	.getRawContactID());
															allContacts
																	.remove(c);
															searchSourceContacts
																	.remove(c);
															contactListAdpater
																	.getContacts()
																	.remove(c);
															contactListAdpater
																	.notifyDataSetChanged();
														}
													})
											.setNegativeButton(R.string.cancel,
													null);
									builder.show();
								}
							}).show();

			return true;
		}

	};

	/**
	 * show or hide soft keyboard (for keyboard button)
	 * 
	 * @param view
	 */
	public void showOrHideKeyboard(View view) {
		closeSysPopupMenu();
		if (isKeyboardShown) {
			hideKeyboard();
		} else {
			showKeyboard();
		}
	}

	/**
	 * backspace search field (for delete button)
	 * 
	 * @param view
	 */
	public void backspace(View view) {
		Editable editable = searchField.getEditableText();
		if (editable != null && editable.length() > 0) {
			editable.delete(editable.length() - 1, editable.length());
		}
	}

	private void showKeyboard() {
		isKeyboardShown = true;
		ImageButton bt = (ImageButton) findViewById(R.id.keyboard_hide_show_bt);
		bt.setImageResource(R.drawable.sym_keyboard_done);

		RelativeLayout topbar = (RelativeLayout) findViewById(R.id.topbar);
		topbar.setVisibility(View.GONE);

		int visibility = keyboardView.getVisibility();
		if (visibility == View.GONE || visibility == View.INVISIBLE) {
			keyboardView.setVisibility(View.VISIBLE);

			keyboardView.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.slide_up_in));
		}
	}

	private void hideKeyboard() {
		isKeyboardShown = false;
		ImageButton bt = (ImageButton) findViewById(R.id.keyboard_hide_show_bt);
		bt.setImageResource(R.drawable.sym_keyboard_show);

		RelativeLayout topbar = (RelativeLayout) findViewById(R.id.topbar);
		topbar.setVisibility(View.VISIBLE);

		int visibility = keyboardView.getVisibility();
		if (visibility == View.VISIBLE) {
			keyboardView.setVisibility(View.GONE);
		}
	}

	/**
	 * group selection button action
	 * 
	 * @param view
	 */
	public void selectGroup(View view) {
		closeSysPopupMenu();
		Intent intent = new Intent(this, GroupSelectActivity.class);
		intent.putExtra("all_contacts_count", allContacts.size());
		startActivityForResult(intent, REQ_GROUP_SELECTION);
		overridePendingTransition(R.anim.slide_up_in, 0);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQ_GROUP_SELECTION:
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					currentGroupID = bundle.getInt("group_id");
					String groupTitle = bundle.getString("group_title");

					refreshContactsInCurrentGroup();

					Button selectGroupBt = (Button) findViewById(R.id.group_select_bt);
					selectGroupBt.setText(groupTitle);
				}
			}
			break;

		case REQ_CONTACT_OP:
			if (resultCode == RESULT_NO_NUMBER) {
				Bundle bundle = data.getExtras();
				String name = bundle.getString("name");
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
						.setMessage(
								getString(R.string.no_number_info) + " ("
										+ name + ")").setNeutralButton(
								R.string.reselect, null);
				builder.show();
			}
			break;

		case REQ_GROUPING:
			// refresh current contacts in the group
			refreshContactsInCurrentGroup();
			break;
		default:
			break;
		}

	}

	private void refreshContactsInCurrentGroup() {
		List<Contact> contacts = null;
		if (currentGroupID == -1) {
			allContacts = cm.getAllContactsByCompoundSort();
			contacts = allContacts;
		} else {
			contacts = cm.getContactsByGroupByCompoundSort(currentGroupID);
		}
		searchSourceContacts = contacts;
		searchContacts(searchField.getEditableText().toString());
	}

	/**
	 * search contacts by search string
	 * 
	 * @param searchStr
	 */
	private void searchContacts(String searchStr) {
		if (searchSourceContacts == null) {
			return;
		}

		if (currentKeyboardType == KeyboardType.number_pad) {
			List<Contact> result = cm.searchByNumber(searchStr,
					searchSourceContacts);
			contactListAdpater.setContacts(result);
		} else if (currentKeyboardType == KeyboardType.qwert_pad) {
			List<Contact> result = cm.searchByName(searchStr,
					searchSourceContacts);
			contactListAdpater.setContacts(result);
		}
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
}