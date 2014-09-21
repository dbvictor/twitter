package com.dvictor.twitter.models;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Tweet {
	private String body;
	private long   uid;
	private String createdAt;
	private User   user;
	
	public static Tweet fromJSON(JSONObject json){
		Tweet tweet = new Tweet();
		// Extract values from JSON to populate the member variables.
		try{
			tweet.body      = json.getString("text");
			tweet.uid       = json.getLong  ("id"  );
			tweet.createdAt = json.getString("created_at");
			tweet.user      = User.fromJSON(json.getJSONObject("user"));
		}catch(JSONException e){
			e.printStackTrace();
			return null;
		}
		return tweet;
	}

	public static ArrayList<Tweet> fromJSON(JSONArray json) {
		ArrayList<Tweet> tweets = new ArrayList<Tweet>(json.length());
		for(int i=0; i<json.length(); i++){
			JSONObject tweetJSON = null;
			try{
				tweetJSON = json.getJSONObject(i);
			}catch(JSONException e){
				e.printStackTrace();
				continue;
			}
			Tweet tweet = Tweet.fromJSON(tweetJSON);
			if(tweet!=null) tweets.add(tweet);
		}
		return tweets;
	}

	public String getBody() {
		return body;
	}

	public long getUid() {
		return uid;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public User getUser() {
		return user;
	}
	
	@Override
	public String toString(){
		return body+" - "+user.getScreenName();
	}
	
}
