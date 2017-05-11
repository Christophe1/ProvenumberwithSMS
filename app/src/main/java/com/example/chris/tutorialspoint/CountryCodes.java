package com.example.chris.tutorialspoint;

import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.tutorialspoint.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CountryCodes extends AppCompatActivity {

    ArrayAdapter<String> adapter;
    ArrayList<String> items;
    ArrayList<String> items2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_codes);
        ListView listView=(ListView)findViewById(R.id.listview_country_codes);
        //for putting into the ListView
        items=new ArrayList<String>();
        //so we can get the code and name of the country
        items2=new ArrayList<String>();
        adapter=new ArrayAdapter(this, R.layout.listview_country_codes_items,R.id.txt,items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               // TextView myTextView = (TextView) view.findViewById(R.id.txt);

                // Split the items2 array in two parts, code and name
                String[] parts = items2.get(i).split(" ");
                //toast the first part of the array - code
                Toast.makeText(getApplicationContext(),parts[0], Toast.LENGTH_SHORT).show();
              //  String text = myTextView.getText().toString();
              //  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG).show();

                //start Main activity, taking the Country Code
                Intent myIntent = new Intent(CountryCodes.this, MainActivity.class);
                myIntent.putExtra("CountryCode", parts[0]);
                CountryCodes.this.startActivity(myIntent);
            }
        });

    }


    public void onStart(){
        super.onStart();

        //  Create json array request
        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(Request.Method.POST,
                "http://www.populisto.com/CountryCodes.php", (JSONArray)null, new Response.Listener<JSONArray>(){
            public void onResponse(JSONArray jsonArray){
                // Successfully got CountryCOdes.php
                // So parse it and populate the listview
                for(int i=0;i<jsonArray.length();i++){
                    try {
                        JSONObject jsonObject=jsonArray.getJSONObject(i);

                        //break the code and name values in the JSON CountryCodes.php into two parts
                        String code = jsonObject.getString("code");
                        String name = jsonObject.getString("name");
                        //add code and name into the items2 array
                        //this way we can isolate the Code separately to put in Main Activity
                        items2.add(code + " " + name);
                        //but for adding to our adapter, the ListView,
                        //we just want name
                        items.add(name);

                      //  items.add(jsonObject.getString("name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("Error", "Unable to parse json array");
            }
        });
        // Create request queue
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        // add json array request to the request queue
        requestQueue.add(jsonArrayRequest);
    }


}
