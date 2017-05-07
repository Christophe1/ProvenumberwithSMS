package com.example.chris.tutorialspoint;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_codes);
        ListView listView=(ListView)findViewById(R.id.listview_country_codes);
        items=new ArrayList<String>();
        adapter=new ArrayAdapter(this, R.layout.listview_country_codes_items,R.id.txt,items);
        listView.setAdapter(adapter);

    }


    public void onStart(){
        super.onStart();

        //  Create json array request
        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(Request.Method.POST,
                "http://www.populisto.com/CountryCodes.php", (JSONArray)null, new Response.Listener<JSONArray>(){
            public void onResponse(JSONArray jsonArray){
                // Successfully download json
                // So parse it and populate the listview
                for(int i=0;i<jsonArray.length();i++){
                    try {
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        items.add(jsonObject.getString("name"));
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
