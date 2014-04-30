package com.example.cloverprimes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PrimeDisplay extends Activity {
	
	ParseObject prevFile;//Database object where our primes are stored.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prime_display);
		
		//Initialize our display objects.
		TextView N = (TextView) findViewById(R.id.textView1);
		ListView primes = (ListView) findViewById(R.id.listView1);
		
		//get the objectId from the sharedPreferrences, at this point,
		//it has undoubtedly already been initialized
		SharedPreferences id = getSharedPreferences("myPrefFile", 0);       
	    String objectId = id.getString("parseID", null);
	     
	    //get our prevFile from our objectId
	    ParseQuery<ParseObject> query = ParseQuery.getQuery("prevFile");
     	try {
     		prevFile = query.get(objectId);
     		
     	} catch (ParseException e1) {
     		e1.printStackTrace();
     	}
     	
     	//get N
     	int n = Integer.parseInt(prevFile.getString("n"));
     	String data = prevFile.getString("primes");
     	
     	//set how many primes we have.
     	N.setText("Total Primes: "+ n);
     	
     	//List that will help us get all of the primes
     	ArrayList<String> list = new ArrayList<String>();     	
     	
     	//tokenizer that splits the string format of our primes: [x1, x2, ..., xN]
     	//into tokens removing '['  ','  ' '  ']', thus leaving only tokens of ints.
     	StringTokenizer reader = new StringTokenizer(data, ",[] ");
     	while(reader.hasMoreTokens())
     	{
     		list.add(""+ reader.nextToken());
     	}
     	
     	//create an adapter to put our list into our listView
     	final StableArrayAdapter adapter = new StableArrayAdapter(this,
     	        android.R.layout.simple_list_item_1, list);
     	    primes.setAdapter(adapter);
	}
	
	//adapts an ArrayAdapter into a usable StableArrayAdapter
	private class StableArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public StableArrayAdapter(Context context, int textViewResourceId,
	        List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }

	    @Override
	    public long getItemId(int position) {
	      String item = getItem(position);
	      return mIdMap.get(item);
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }

	  }



}
