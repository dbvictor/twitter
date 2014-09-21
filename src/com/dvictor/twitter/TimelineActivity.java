package com.dvictor.twitter;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.dvictor.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

public class TimelineActivity extends Activity {
	private TwitterClient     client;
	private ArrayList<Tweet>  tweets;
	private TweetArrayAdapter aTweets;
	private ListView		  lvTweets;
	private long              lastItemId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		client = TwitterApp.getRestClient();
		lastItemId = 0; // Always start from 0.
		populateTimeline();
		lvTweets = (ListView) findViewById(R.id.lvTweets);
		tweets = new ArrayList<Tweet>();
		aTweets = new TweetArrayAdapter(this, tweets);
		lvTweets.setAdapter(aTweets);
		lvTweets.setOnScrollListener(new EndlessScrollListener() {
			/** The endless scroll listener will call us whenever its count says that we need more.  We don't care what page it is on, we just get more. */
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				populateTimeline(); 
			}
		});
	}
	
	public void populateTimeline(){
		Log.d("DVDEBUG", "+ TimelineActivity.populateTimeline()");
		client.getHomeTimeline(lastItemId, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(JSONArray json) {
				Log.d("json", "Home Timeline JSON: "+json.toString());
				aTweets.addAll(Tweet.fromJSON(json));
				lastItemId = tweets.get(tweets.size()-1).getUid(); // record the last item ID we've seen now, so we know where to continue off from next time.
			}
			
			@Override
			public void onFailure(Throwable e, String s) {
				Log.d("debug", e.toString());
				Log.d("debug", s.toString());
			}
		});
	}
}
