package com.paphus.sdk.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.paphus.sdk.activity.R;
import com.paphus.sdk.BOTlibreCredentials;
import com.paphus.sdk.FORUMSlibreCredentials;
import com.paphus.sdk.LIVECHATlibreCredentials;
import com.paphus.sdk.PaphusCredentials;
import com.paphus.sdk.SDKConnection;
import com.paphus.sdk.activity.actions.HttpAction;
import com.paphus.sdk.activity.actions.HttpConnectAction;
import com.paphus.sdk.activity.actions.HttpFetchAction;
import com.paphus.sdk.activity.actions.HttpGetCategoriesAction;
import com.paphus.sdk.activity.actions.HttpGetImageAction;
import com.paphus.sdk.activity.actions.HttpGetInstancesAction;
import com.paphus.sdk.activity.actions.HttpGetTagsAction;
import com.paphus.sdk.activity.forum.ForumBrowseActivity;
import com.paphus.sdk.activity.livechat.ChannelBrowseActivity;
import com.paphus.sdk.config.AvatarConfig;
import com.paphus.sdk.config.BotModeConfig;
import com.paphus.sdk.config.BrowseConfig;
import com.paphus.sdk.config.ChannelConfig;
import com.paphus.sdk.config.ContentConfig;
import com.paphus.sdk.config.DomainConfig;
import com.paphus.sdk.config.ForumConfig;
import com.paphus.sdk.config.ForumPostConfig;
import com.paphus.sdk.config.InstanceConfig;
import com.paphus.sdk.config.LearningConfig;
import com.paphus.sdk.config.UserConfig;
import com.paphus.sdk.config.VoiceConfig;
import com.paphus.sdk.config.WebMediumConfig;

/**
 * Main view, allows connect, browse and content creation.
 * Also stores a lot of shared data, such as the connection, current user/instance/conversation.
 * You do not need to use the MainActivity in your app, but it needs to be there so the other activities can access the shared data.
 * You can reuse any of the activities in your own app, such as just the Chat, Forum, LiveChat, or user management activities.
 * You can customize the code and layouts any way you wish for your own app, or just use the SDKConnection, or LiveChatConnection API.
 * <p>
 * You can create an app to access a single bot, forum, or channel instance using this MainActivity class.
 * You will need to create your bot, forum, or channel using yuor service provider website, or mobile app (BOT libre, FORUMS libre, LIVE CHAT libre, Paphus Live Chat).
 * <p>
 * You only need to set the applicationId, launchType, and launchInstanceId or launchInstanceName.
 * You will also want to replace the logo.png in res/drawable and update the application name and version in the AndroidManifest,
 * then you can package the app into your own apk file and upload it to Google Play or any other site.
 * The app is yours, you can charge for it, or give it away for free.
 */
public class MainActivity extends Activity {
	public static final boolean DEBUG = false;
	public static final boolean ADULT = false;
	
	/**
	 * Enter your application ID here.
	 * You can get an application ID from any of the services websites (BOT libre, FORUMS libre, LIVE CHAT libre, Paphus Live Chat)
	 */
	public static String applicationId = "";
	/**
	 * Configure your connection credentials here.
	 * Choose which service provider you wish to connect to.
	 */
	public static SDKConnection connection;
	/**
	 * To launch into a specific domain, enter the domain id here.
	 */
	public static String domainId = null;
	public static DomainConfig domain;
	/**
	 * Choose your service provider using the correct credentials.
	 */
	static {
		connection = new SDKConnection(new BOTlibreCredentials(applicationId));
		//connection = new SDKConnection(new FORUMSlibreCredentials(applicationId));
		//connection = new SDKConnection(new LIVECHATlibreCredentials(applicationId));
		//connection = new SDKConnection(new PaphusCredentials(applicationId));
		if (domainId != null) {
			domain = new DomainConfig();
			domain.id = domainId;
			connection.setDomain(domain);
		}
	}
	/**
	 * If you are building a single instance app, then you can set the instance id or name here,
	 * and use this activity to launch it.
	 */
	public static String launchInstanceId = ""; // i.e. "171"
	public static String launchInstanceName = ""; // i.e. "Help Bot"
	/**
	 * If you are building a single instance app, then you can set the launchType to
	 * have this activity launch the bot, forum, or channel.
	 */
	public static LaunchType launchType = LaunchType.Browse;
	public enum LaunchType {Browse, Bot, Forum, Channel}
	
	public static WebMediumConfig instance;
	public static ForumPostConfig post;
	public static UserConfig user;
	public static UserConfig viewUser;
	public static String type = "Bots";
	public static BotModeConfig botMode = new BotModeConfig();
	public static VoiceConfig voice = new VoiceConfig();
	public static LearningConfig learning = new LearningConfig();
	public static String conversation;
	public static String template = "";
	public static Object[] templates;
	public static Object[] tags;
	public static Object[] categories;
	public static Object[] forumTags;
	public static Object[] forumPostTags;
	public static Object[] forumCategories;
	public static Object[] channelTags;
	public static Object[] channelCategories;
	public static boolean showImages = true;
	public static List<WebMediumConfig> instances = new ArrayList<WebMediumConfig>();
	public static List<ForumPostConfig> posts = new ArrayList<ForumPostConfig>();
	public static List<AvatarConfig> avatars;
	public static List<AvatarConfig> sharedAvatars;
	public static MainActivity current;
	public static boolean wasDelete;
	public static String[] types = new String[]{"Bots", "Forums", "Live Chat", "Domains"};
	public static String[] channelTypes = new String[]{"ChatRoom", "OneOnOne"};
	public static String[] accessModes = new String[]{"Everyone", "Users", "Members", "Administrators"};
	public static String[] learningModes = new String[]{"Disabled", "Administrators", "Users", "Everyone"};
	public static String[] correctionModes = new String[]{"Disabled", "Administrators", "Users", "Everyone"};
	public static String[] botModes = new String[]{"ListenOnly", "AnswerOnly", "AnswerAndListen"};

	Menu menu;

	public static String getRealPathFromURI(Context context, Uri uri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(uri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	public static void error(String message, Exception exception, Activity activity) {
		try {
			if (DEBUG) {
				System.out.println(String.valueOf(message));
				if (exception != null) {
					exception.printStackTrace();
				}
			}
			if (message == null) {
				message = "";
			}
			if (message.contains("<html>")) {
				message = "Server Error, ensure you are connected to the Internet";
			}
			MainActivity.showMessage(message, activity);
		} catch (Throwable error) {
			error.printStackTrace();
		}
	}
	
	public static void showMessage(String message, Activity activity) {
		AlertDialog dialog = new AlertDialog.Builder(activity).create();
		dialog.setMessage(message);
		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {}
		    });
		dialog.show();
	}
	
	public static void prompt(String message, Activity activity, EditText text, DialogInterface.OnClickListener listener) {
		AlertDialog dialog =  new AlertDialog.Builder(activity).create();
		dialog.setMessage(message);
		dialog.setView(text);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", listener);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    });
		dialog.show();
	}
	
	public static void confirm(String message, Activity activity, DialogInterface.OnClickListener listener) {
		AlertDialog dialog =  new AlertDialog.Builder(activity).create();
		dialog.setMessage(message);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", listener);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    });
		dialog.show();
	}
	
	public static Object[] getAllTags(Activity activity) {
		if (tags == null) {
		    try {
		    	ContentConfig config = new ContentConfig();
		    	config.type = "Bot";
		    	HttpGetTagsAction action = new HttpGetTagsAction(activity, config);
		    	action.postExecute(action.execute().get());
				if (action.getException() != null) {
					tags = new String[0];
				}
		    } catch (Exception ignore) {}
		}
		return tags;
	}
	
	public static Object[] getAllForumTags(Activity activity) {
		if (forumTags == null) {
		    try {
		    	ContentConfig config = new ContentConfig();
		    	config.type = "Forum";
		    	HttpGetTagsAction action = new HttpGetTagsAction(activity, config);
		    	action.postExecute(action.execute().get());
				if (action.getException() != null) {
					forumTags = new String[0];
				}
		    } catch (Exception ignore) {}
		}
		return forumTags;
	}
	
	public static Object[] getAllForumPostTags(Activity activity) {
		if (forumPostTags == null) {
		    try {
		    	ContentConfig config = new ContentConfig();
		    	config.type = "Post";
		    	HttpGetTagsAction action = new HttpGetTagsAction(activity, config);
		    	action.postExecute(action.execute().get());
				if (action.getException() != null) {
					forumPostTags = new String[0];
				}
		    } catch (Exception ignore) {}
		}
		return forumPostTags;
	}
	
	public static Object[] getAllChannelTags(Activity activity) {
		if (channelTags == null) {
		    try {
		    	ContentConfig config = new ContentConfig();
		    	config.type = "Channel";
		    	HttpGetTagsAction action = new HttpGetTagsAction(activity, config);
		    	action.postExecute(action.execute().get());
				if (action.getException() != null) {
					channelTags = new String[0];
				}
		    } catch (Exception ignore) {}
		}
		return channelTags;
	}
	
	public static Object[] getAllCategories(Activity activity) {
		if (categories == null) {
		    try {
		    	ContentConfig config = new ContentConfig();
		    	config.type = "Bot";
		    	HttpGetCategoriesAction action = new HttpGetCategoriesAction(activity, config);
		    	action.postExecute(action.execute().get());
				if (action.getException() != null) {
					categories = new String[0];
				}
		    } catch (Exception ignore) {}
		}
		return categories;
	}
	
	public static Object[] getAllForumCategories(Activity activity) {
		if (forumCategories == null) {
		    try {
		    	ContentConfig config = new ContentConfig();
		    	config.type = "Forum";
		    	HttpGetCategoriesAction action = new HttpGetCategoriesAction(activity, config);
		    	action.postExecute(action.execute().get());
				if (action.getException() != null) {
					forumCategories = new String[0];
				}
		    } catch (Exception ignore) {}
		}
		return forumCategories;
	}
	
	public static Object[] getAllChannelCategories(Activity activity) {
		if (channelCategories == null) {
		    try {
		    	ContentConfig config = new ContentConfig();
		    	config.type = "Channel";
		    	HttpGetCategoriesAction action = new HttpGetCategoriesAction(activity, config);
		    	action.postExecute(action.execute().get());
				if (action.getException() != null) {
					channelCategories = new String[0];
				}
		    } catch (Exception ignore) {}
		}
		return channelCategories;
	}
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	
		current = this;
		
		if (user == null) {
	    	SharedPreferences cookies = getPreferences(Context.MODE_PRIVATE);
	    	String user = cookies.getString("user", null);
	    	String token = cookies.getString("token", null);
	
	    	if ((user != null) && (token != null)) {
	    		UserConfig config = new UserConfig();
	    		config.user = user;
	    		config.token = token;
	    		HttpConnectAction action = new HttpConnectAction(this, config, false);
		    	action.execute();
	    	}
		}

		if ((launchType == LaunchType.Bot) || (launchType == LaunchType.Channel))  {
			setContentView(R.layout.activity_main_chat);			
		} else if (launchType == LaunchType.Forum)  {
			setContentView(R.layout.activity_main_browse);			
		} else {
			setContentView(R.layout.activity_main);
		}
		
		resetView();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			ArrayAdapter adapter = new ArrayAdapter(this,
	                android.R.layout.simple_spinner_dropdown_item, MainActivity.types);
			spin.setAdapter(adapter);
			spin.setSelection(Arrays.asList(MainActivity.types).indexOf(MainActivity.type));
			
		}
		if (domain != null) {
			setTitle(domain.name);
	        HttpGetImageAction.fetchImage(this, domain.avatar, (ImageView)findViewById(R.id.splash));			
		}
		resetMenu();
	}
	
	@Override
	public void onResume() {
		if (user != null) {
			resetView();
	    	
	    	SharedPreferences.Editor cookies = getPreferences(Context.MODE_PRIVATE).edit();
	    	cookies.putString("user", MainActivity.user.user);
	    	cookies.putString("token", MainActivity.user.token);
	    	cookies.commit();
		}
		super.onResume();
	}

	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu_user, menu);
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.menu = menu;
		resetMenu();
	    return true;
	}
	
	public void resetMenu() {
		if (this.menu == null) {
			return;
		}
        for (int index = 0; index < this.menu.size(); index++) {
    	    menu.getItem(index).setEnabled(true);        	
        }
        if (MainActivity.user == null) {
        	this.menu.findItem(R.id.menuSignOut).setEnabled(false);
        	this.menu.findItem(R.id.menuViewUser).setEnabled(false);
        	this.menu.findItem(R.id.menuEditUser).setEnabled(false);
        } else {
        	this.menu.findItem(R.id.menuSignIn).setEnabled(false);
        	this.menu.findItem(R.id.menuSignUp).setEnabled(false);
        }
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         
        switch (item.getItemId())
        {        
	    case R.id.menuSignIn:
	    	login();
	        return true;
        case R.id.menuSignUp:
        	createUser();
            return true;
        case R.id.menuSignOut:
        	logout();
            return true;
        case R.id.menuEditUser:
        	editUser();
            return true;
        case R.id.menuViewUser:
        	viewUser();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	public void login() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

	public void logout() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		
		connection.disconnect();
        user = null;
        instance = null;
        avatars = null;
        conversation = null;
        post = null;
        posts = new ArrayList<ForumPostConfig>();
        instances = new ArrayList<WebMediumConfig>();
        domain = null;
        tags = null;
        categories= null;
        learning = null;
        voice = null;

    	SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
    	editor.putString("user", null);
    	editor.putString("token", null);
    	editor.putString("instance", null);
    	editor.commit();
    	
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

	public void menu(View view) {
		openOptionsMenu();
	}

	public void createUser() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivity(intent);
	}

	public void editUser() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		
        Intent intent = new Intent(this, EditUserActivity.class);
        startActivity(intent);
	}

	public void viewUser() {
		viewUser = user;
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		
        Intent intent = new Intent(this, ViewUserActivity.class);
        startActivity(intent);
	}

	public void browse(View view) {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		type = (String)spin.getSelectedItem();
		if (type == null) {
			type = "Bots";
		}
		if (type.equals("Bots")) {
	        Intent intent = new Intent(this, BrowseActivity.class);
	        startActivity(intent);			
		} else if (type.equals("Forums")) {
	        Intent intent = new Intent(this, ForumBrowseActivity.class);
	        startActivity(intent);			
		} else if (type.equals("Live Chat")) {
	        Intent intent = new Intent(this, ChannelBrowseActivity.class);
	        startActivity(intent);			
		} else if (type.equals("Domains")) {
			BrowseConfig config = new BrowseConfig();
			config.type = "Domain";
			
			HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
			action.execute();
		}
	}
	
	/**
	 * Start a chat session with the hard coded instance.
	 */
	public void launch(View view) {
		WebMediumConfig config = null;
		if (launchType == LaunchType.Bot) {
			config = new InstanceConfig();
		} else if (launchType == LaunchType.Forum) {
			config = new ForumConfig();
		} else if (launchType == LaunchType.Channel) {
			config = new ChannelConfig();
		}
        config.id = launchInstanceId;
        config.name = launchInstanceName;
		
        HttpAction action = new HttpFetchAction(this, config, true);
    	action.execute();    	
    }
}
