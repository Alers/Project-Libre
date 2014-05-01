package com.paphus.sdk.activity.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.paphus.sdk.activity.ChatActivity;
import com.paphus.sdk.activity.DomainActivity;
import com.paphus.sdk.activity.InstanceActivity;
import com.paphus.sdk.activity.MainActivity;
import com.paphus.sdk.activity.forum.ForumActivity;
import com.paphus.sdk.activity.livechat.ChannelActivity;
import com.paphus.sdk.activity.livechat.LiveChatActivity;
import com.paphus.sdk.config.ChannelConfig;
import com.paphus.sdk.config.DomainConfig;
import com.paphus.sdk.config.ForumConfig;
import com.paphus.sdk.config.InstanceConfig;
import com.paphus.sdk.config.WebMediumConfig;

public class HttpFetchAction extends HttpUIAction {
	WebMediumConfig config;
	boolean launch;

	public HttpFetchAction(Activity activity, WebMediumConfig config) {
		super(activity);
		this.config = config;
	}
	
	public HttpFetchAction(Activity activity, WebMediumConfig config, boolean launch) {
		super(activity);
		this.config = config;
		this.launch = launch;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.config = MainActivity.connection.fetch(this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
		try {
			MainActivity.instance = this.config;
			
        	SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
        	cookies.putString(this.config.getType(), this.config.name);
        	cookies.commit();

        	Class childActivity = null;
        	if (this.launch) {
	        	if (this.config instanceof ChannelConfig) {
	        		childActivity = LiveChatActivity.class;
	        	} else if (this.config instanceof ForumConfig) {
	        		childActivity = ForumActivity.class;
	        	} else if (this.config instanceof InstanceConfig) {
	        		childActivity = ChatActivity.class;
	        		
	        		HttpAction action = new HttpGetVoiceAction(this.activity, (InstanceConfig)MainActivity.instance.credentials());
	        		action.execute();
	        	} else if (this.config instanceof DomainConfig) {
	        		childActivity = DomainActivity.class;
	        		MainActivity.connection.setDomain((DomainConfig)this.config);
	    			MainActivity.domain = (DomainConfig)this.config;
	    			MainActivity.tags = null;
	    			MainActivity.categories = null;
	    			MainActivity.forumTags = null;
	    			MainActivity.forumCategories = null;
	    			MainActivity.channelTags = null;
	    			MainActivity.channelCategories = null;
	        	}        		
        	} else {
	        	if (this.config instanceof ChannelConfig) {
	        		childActivity = ChannelActivity.class;
	        	} else if (this.config instanceof ForumConfig) {
	        		childActivity = ForumActivity.class;
	        	} else if (this.config instanceof InstanceConfig) {
	        		childActivity = InstanceActivity.class;
	        		
	        		HttpAction action = new HttpGetVoiceAction(this.activity, (InstanceConfig)MainActivity.instance.credentials());
	        		action.execute();
	        	} else if (this.config instanceof DomainConfig) {
	        		childActivity = DomainActivity.class;
	        		MainActivity.connection.setDomain((DomainConfig)this.config);
	    			MainActivity.domain = (DomainConfig)this.config;
	    			MainActivity.tags = null;
	    			MainActivity.categories = null;
	    			MainActivity.forumTags = null;
	    			MainActivity.forumCategories = null;
	    			MainActivity.channelTags = null;
	    			MainActivity.channelCategories = null;
	        	}
        	}
	        Intent intent = new Intent(this.activity, childActivity);
	        this.activity.startActivity(intent);
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
	}
	
}