package com.example.chris.tutorialspoint;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.tutorialspoint.R;

public class PopulistoContactList extends AppCompatActivity {

    private TextView textphonenumber;
    private String strphone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_populisto_contact_list);

        textphonenumber=(TextView) findViewById(R.id.textView3);

        Intent myIntent =this.getIntent();
        //phone number of the user
        strphone = myIntent.getStringExtra("keyName");

        textphonenumber.setText(strphone);
    }
}
