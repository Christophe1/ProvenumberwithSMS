package com.example.chris.tutorialspoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.tutorialspoint.R;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.HashMap;
import java.util.Map;

import static com.example.tutorialspoint.R.id.txtphoneNoofUser;

public class MainActivity extends AppCompatActivity  {

    // this is the php file name where to insert into the database, the user's phone number
    private static final String REGISTER_URL = "http://www.populisto.com/insert.php";

    //we are posting phoneNoofUser, which in PHP is phonenumber
    public static final String KEY_PHONENUMBER = "phonenumberofuser";


    //related to SMS verification
    Button btnSendSMS;

    Button buttonRegister;

    EditText txtphoneNoofUser;

    String phoneNoofUser;
    String origNumber;
    TextView txtSelectCountry;

    private BroadcastReceiver receiver;

    TextView txtCountryCode;
    String CountryCode;
    String phoneNoofUserbeforeE164;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSendSMS = (Button) findViewById(R.id.btnSendSMS);


        txtphoneNoofUser = (EditText) findViewById(R.id.txtphoneNoofUser);

       buttonRegister = (Button) findViewById(R.id.buttonRegister);

        txtSelectCountry = (TextView) findViewById(R.id.txtSelectCountry);

        //buttonRegister.setOnClickListener(this);

        //  when the form loads, check to see if phoneNoofUser is in there
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String phoneNoofUserCheck = sharedPreferences.getString("phonenumberofuser","");

        //  if it is not in there, go through verification
        if ( phoneNoofUserCheck == null || phoneNoofUserCheck.equals("") ) {


            btnSendSMS.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    System.out.println("you clicked it, send message");
                    sendSMSMessage();
                }
            });


        }
        else {
            // if it is registered then start the next activity
            Intent myIntent = new Intent(MainActivity.this, PopulistoContactList.class);
            myIntent.putExtra("keyName", phoneNoofUserCheck);
            MainActivity.this.startActivity(myIntent);


        }


        buttonRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                System.out.println("you clicked it, register");
                phoneNoofUser = txtphoneNoofUser.getText().toString();
                registerUser();
            }
        });

        //when 'Select Country' Text is clicked
        //load the new activity CountryCodes showing the list of all countries
        txtSelectCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(MainActivity.this, CountryCodes.class);
               // myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });

        txtCountryCode =(TextView) findViewById(R.id.txtCountryCode);
        Intent myIntent =this.getIntent();
        //put in the Country code selected by the user in CountryCodes.java
        CountryCode = myIntent.getStringExtra("CountryCode");
        txtCountryCode.setText(CountryCode);
    }




    protected void sendSMSMessage() {

        IntentFilter filter = new IntentFilter();
//        the thing we're looking out for is received SMSs
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");

        //this is to check the incoming text message
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)

            {
                Bundle extras = intent.getExtras();

                if (extras == null)
                    return;

                Object[] pdus = (Object[]) extras.get("pdus");
                SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdus[0]);
                origNumber = msg.getOriginatingAddress();

                Toast.makeText(getApplicationContext(), "Originating number" + origNumber, Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "Sent to number" + phoneNoofUser, Toast.LENGTH_LONG).show();

                //when the text message is received, see if originating number matches the
                //sent to number
                if (origNumber.equals(phoneNoofUser)) {
                    //save the phone number so this process is skipped in future
                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("phonenumberofuser", phoneNoofUser);
                    editor.commit();

                    //Here we want to add the user's phone number to the user table
                    //using Volley

                    //start next activity, taking the phone number
                    Intent myIntent = new Intent(MainActivity.this, PopulistoContactList.class);
                    myIntent.putExtra("keyName", phoneNoofUser);
                    MainActivity.this.startActivity(myIntent);

                }

                else {
                    Toast.makeText(getApplicationContext(), "Number not correct.", Toast.LENGTH_LONG).show();

                }
            }

        };
        registerReceiver(receiver, filter);

        //this is the number the user enters in the Phone Number textbox
        //We need to parse this, to make it into E.164 format
        phoneNoofUserbeforeE164 = txtphoneNoofUser.getText().toString();
        phoneNoofUser = String.valueOf(CountryCode) +  String.valueOf(phoneNoofUserbeforeE164);

        //phoneNoofUser = txtphoneNoofUser.getText().toString();

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            //For the second parameter, CountryCode, put whatever country code the user picks
            //we pass it through phoneUtil to get rid of first 0 like in +353087 etc
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNoofUser, CountryCode);
            phoneNoofUser = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
            //Since you know the country you can format it as follows:
            //System.out.println(phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164));
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        //this is the text of the SMS received
        String message = "Verification test code. Please ignore this message. Thank you.";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNoofUser, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();

        }

        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    public void onClick(View v) {
        phoneNoofUser = txtphoneNoofUser.getText().toString();
        if(v== buttonRegister){
            System.out.println("you clicked it");
            registerUser();
        }
    }

    private void registerUser() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();

                    }

                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_PHONENUMBER, phoneNoofUser);
                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this) ;
        requestQueue.add(stringRequest);
    }





    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}