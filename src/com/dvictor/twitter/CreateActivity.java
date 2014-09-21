package com.dvictor.twitter;

import org.json.JSONObject;

import com.dvictor.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CreateActivity extends Activity {
	private TwitterClient client;
	private Tweet tweet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);
		client = TwitterApp.getRestClient();
	}
	
	public void create(View v){
		EditText etBody = (EditText) findViewById(R.id.etNewTweet);
		String etBodyText = etBody.getText().toString();
		// If empty, don't allow send.
		if((etBodyText==null)||(etBodyText.trim().length()<=0)){
			Toast.makeText(this, "Nothing to Post!", Toast.LENGTH_SHORT).show();
		// Else send tweet!
		}else{
			// 1. Send text to Twitter
			final CreateActivity parentThis = this;
			client.createTweet(etBody.getText().toString(), new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(JSONObject json) {
					Log.d("json", "Created JSON: "+json.toString());
					tweet = Tweet.fromJSON(json);
					Toast.makeText(parentThis, "Posted", Toast.LENGTH_SHORT).show();
					// 2. Return result to timeline activity
					Intent i = new Intent();
					i.putExtra("tweet", tweet);
					setResult(RESULT_OK, i);
					finish();
				}
				@Override
				public void onFailure(Throwable e, String s) {
					Log.d("debug", e.toString());
					Log.d("debug", s.toString());
					Toast.makeText(parentThis, "FAILED!", Toast.LENGTH_SHORT).show();
					// Don't return to timeline.  Allow them a chance to retry.  They can always hit the back button.
				}
			});
		}
	}
}
