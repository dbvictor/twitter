package com.dvictor.twitter;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.dvictor.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

public class TimelineActivity extends Activity {
	private TwitterClient      client;
	private ArrayList<Tweet>   tweets;
	private TweetArrayAdapter  aTweets;
	private ListView		   lvTweets;
	private long               lastItemId;
	private boolean            internetEnabled;
	private SwipeRefreshLayout swipeContainer;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		setupSwipeContainer();
		internetEnabled = true;
		client = TwitterApp.getRestClient();
		lastItemId = 0; // Always start from 0.
		populateTimeline(true);
		lvTweets = (ListView) findViewById(R.id.lvTweets);
		tweets = new ArrayList<Tweet>();
		aTweets = new TweetArrayAdapter(this, tweets);
		lvTweets.setAdapter(aTweets);
		setupEndlessScroll();
	}
	
	/** Setup swipe down to refresh. */
	private void setupSwipeContainer(){
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                populateTimeline(true); // true = start from beginning again
                setupEndlessScroll(); // Resetup endless scroll in case it previously hit the bottom and stopped scrolling further again. 
            } 
        });
        // Configure the refreshing colors
        swipeContainer.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);		
	}
	
	private void setupEndlessScroll(){
		lvTweets.setOnScrollListener(new EndlessScrollListener() {
			/** The endless scroll listener will call us whenever its count says that we need more.  We don't care what page it is on, we just get more. */
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				populateTimeline(false); 
			}
		});
	}
	
	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_timeline, menu);
		return true;
	}

	/** Menu selection to turn on/off internet to simulate offline. */
	public void internetToggle(MenuItem menuItem){
		internetEnabled = !internetEnabled;
		if(internetEnabled){
			Toast.makeText(this, "Internet ON", Toast.LENGTH_SHORT).show();
			menuItem.setIcon(R.drawable.ic_action_internet_off);
			setupEndlessScroll(); // Re-enable endless scrolling because if it hit end before, it would not try again.
		}else{
			Toast.makeText(this, "Internet OFF", Toast.LENGTH_SHORT).show();
			menuItem.setIcon(R.drawable.ic_action_internet_on);
		}
	}
	
	/** Menu selection to create a new tweet. */
	public void create(MenuItem menuItem){
		if(!isNetworkAvailable()){ // If no network, don't allow create tweet.
			Toast.makeText(this, "Network Not Available!", Toast.LENGTH_SHORT).show();
		}else{
			//Toast.makeText(this, "Settings!", Toast.LENGTH_SHORT).show();
			Intent i = new Intent(this,CreateActivity.class);
			//no args: i.putExtra("settings", searchFilters);
			startActivityForResult(i, 1);
		}
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if(requestCode==1){ // CreateActivity Result
    		if(resultCode == RESULT_OK){
    			Tweet tweet = (Tweet) data.getSerializableExtra("tweet");
    			if(tweet!=null){
    				aTweets.insert(tweet, 0);
    				Toast.makeText(this, "Timeline Updated", Toast.LENGTH_SHORT).show();    				
    			}else Toast.makeText(this, "MISSING RESULT", Toast.LENGTH_SHORT).show();    				
    		}
    	}
    }
	
    /** Populate the timeline with more results */
	public void populateTimeline(final boolean refresh){
		Log.d("DVDEBUG", "+ TimelineActivity.populateTimeline()");
		if(!isNetworkAvailable()){ // If no network, don't allow create tweet.
			Toast.makeText(this, "Network Not Available!", Toast.LENGTH_SHORT).show();
		}else{
			if(refresh) lastItemId = 0; // If told to refresh from beginning, start again from 0.
			client.getHomeTimeline(lastItemId, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(JSONArray json) {
					Log.d("json", "Home Timeline JSON: "+json.toString());
					if(refresh) aTweets.clear(); // If told to refresh from beginning, clear existing results 
					aTweets.addAll(Tweet.fromJSON(json));
					lastItemId = tweets.get(tweets.size()-1).getUid(); // record the last item ID we've seen now, so we know where to continue off from next time.
	                // Now we call setRefreshing(false) to signal refresh has finished
	                swipeContainer.setRefreshing(false);
				}
				@Override
				public void onFailure(Throwable e, String s) {
					Log.d("debug", e.toString());
					Log.d("debug", s.toString());
				}
			});
		}
	}
	
    private Boolean isNetworkAvailable() {
    	if(!internetEnabled) return false; // If simulated off, make it appaer not working.
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
	
}
