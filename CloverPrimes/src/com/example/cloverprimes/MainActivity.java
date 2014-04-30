package com.example.cloverprimes;


import java.util.Arrays;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity  {
	
	int n; //our currently requested N
	int nPrev; //the previously requested N	
	boolean isInt; //flag to detemine if N is an int		
	ParseObject prevFile; //our database object where our previous data is stored.
	String objectId; //id of the database object we need. This is saved in the app's SharedPreferrences
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //get objectId, if null, we create a new prevFile.
        SharedPreferences id = getSharedPreferences("myPrefFile", 0);        
        objectId = id.getString("parseID", null);       
        //initialize our database.
        Parse.initialize(this, "85Iz63VyVHpnpWnwKfkl1YHXttOrWVUrepbrek6I", "jdscJtf8MLfPHRPcVFtVxkVGcDPRObkPpztJ0ydb");
        //if null, create a new prevFile.
        if(objectId == null)
        {
        	
        	//new prevFile
        	prevFile = new ParseObject("prevFile");
        	prevFile.put("n", ""+ 0);
        	//save it
        	try {
				prevFile.save();
			} catch (ParseException e) {				
				e.printStackTrace();
			}
        	
        	//store the objectId in the app preferrences.
        	SharedPreferences.Editor edit = id.edit();
        	edit.putString("parseID", prevFile.getObjectId());        	
        	edit.commit();
        	
        	//no previous N, so set to 0.
        	nPrev = 0;
        }
        //get the corresponding prevFile with our objectId
        else
        {
        	//get prevFile.
        	ParseQuery<ParseObject> query = ParseQuery.getQuery("prevFile");
        	try {
        		prevFile = query.get(objectId);
        		
        	} catch (ParseException e1) {
        		e1.printStackTrace();
        	}        	
        	//get the length.
        	String prevLength = prevFile.getString("n");       	
        	
        	if(prevLength == null)
        		nPrev = 0;
        	else
        		nPrev = Integer.parseInt(prevLength);        	
        }

        //initialize our displayed objects
        Button calculate = (Button) findViewById(R.id.button1);
        Button prev = (Button) findViewById(R.id.button2);       
        EditText N = (EditText) findViewById(R.id.editText1);
        
        //N is not yet an int
        isInt = false;       	
        
        //wait until user inputs an N        
        N.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            	
            	try{
            		//only accept if it's a valid N
            		n =  Integer.parseInt(s.toString());
            		if(n<=0)
            			isInt = false;
            		else
            			isInt = true;  
            	}
            	catch(NumberFormatException e){
            		isInt = false;            		
            	}
            	              
            }
        });
       
        //intent to be used to display primes.
        final Intent intent = new Intent(this,PrimeDisplay.class);
        
        //Calculate the sequence of N prime numbers.
        calculate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(!isInt)//if N is not a valid int, make them input a valid int
            	{
            		Context context = getApplicationContext();
            		CharSequence text = "Please enter a valid N!";
            		int duration = Toast.LENGTH_SHORT;

            		Toast toast = Toast.makeText(context, text, duration);
            		toast.show();
            	}
            	else
            	{
            		//runs the algorithm to get prime numbers, then switches activity.
            		runAlgorithm(n);
            		startActivity(intent);           		
            	}                
            }
        });
        
        
        //show previous calculation!
        prev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(nPrev == 0) //if there wasn't previous calculation, let them know.
            	{
            		Context context = getApplicationContext();
            		CharSequence text = "There is no previous calculation!";
            		int duration = Toast.LENGTH_SHORT;

            		Toast toast = Toast.makeText(context, text, duration);
            		toast.show();
            	}
            	else
            	{            		
            		//no need to run algorithm again, we already have our primes in database.
            		startActivity(intent);            		
            	}                
            }
        });
    }
    
    /*
     * Checks the set of all integers>1 for primes, puts them in primes[], and adds this to database.
     */
    public void runAlgorithm(int n)
    {
    	
    	//find N primes:
    	int primes[] = new int[n];
    	int m = 2;
    	//loop N times
    	for(int x = 0; x< n; x++) 
    	{
    		boolean notPrime = true;
    		//loop through the set of all integers from the previous
    		//prime, up to the next prime.
    		while(notPrime)
    		{
    			//checkPrime is where the math happens
    			if(checkPrime(m))
    			{
    				primes[x] = m;
    				notPrime = false;
    			}    			
    			else
    				notPrime = true;
    			m++;    				
    		}    		
    	}
    	
    	//nPrev is now N
    	nPrev = n;
    	
    	//add our calculations into the database
        prevFile.put("n", ""+ n);
        prevFile.put("primes",Arrays.toString(primes));
        prevFile.saveInBackground();    
    }
    /*
     * Checks integer m to see if it is prime. This algorithm is similar to a brute force
     * algorithm (which checks whether m is divisible by any number by iterating through
     * all positive integers <m), except, it modifies the limit m. the limit first starts at m, as if it was
     * the normal brute force algorithm, except that when it finds that a number m isn't divisible by 
     * the set of positive integers <n (except for 1), it realizes m wont be divisible by any number >(m/n), because any number
     * >(m/n) would need a partner that is <n (and not 1). This reduces the algorithms run time from O(N) to 
     * approximately O(logN).
     */
    private boolean checkPrime(int m)
    {
    	boolean isPrime = true;
    	
    	if(m==2)//hard-code the '2' case.
    		return true;
    	
    	int limit = m;//if m is not divisible by anything in the first m numbers, then it is not divisible by anything.    	
    	int x = 2;//start at 2.
    	while(x<=limit)
    	{
    		if(m%x == 0)//if m is divisible by n, it is not prime.
    		{
    			isPrime = false;
    			
    			x=limit+1;
    		}
    		else//reduce limit, keep looking.
    		{
    			limit = m/x;
    			x++;
    		}    		
    	}
    	
    	return isPrime;
    }


    
    
}
