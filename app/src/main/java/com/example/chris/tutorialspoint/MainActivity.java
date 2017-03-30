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
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tutorialspoint.R;

public class MainActivity extends Activity {
    Button sendBtn;
    EditText txtphoneNo;

    String phoneNo;
    String origNumber;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendBtn = (Button) findViewById(R.id.btnSendSMS);
        txtphoneNo = (EditText) findViewById(R.id.editText);

        //  when the form loads, check to see if phoneNo is in there
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String phoneNoCheck = sharedPreferences.getString("phonenumber","");

        if ( phoneNoCheck == null || phoneNoCheck.equals("") ) {
            //  if it is not in there, go through verification

            sendBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    sendSMSMessage();
                }
            });

            IntentFilter filter = new IntentFilter();
//        the thing we're looking out for is received SMSs
            filter.addAction("android.provider.Telephony.SMS_RECEIVED");

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
                    Toast.makeText(getApplicationContext(), "Sent to number" + phoneNo, Toast.LENGTH_LONG).show();

                }

            };
            registerReceiver(receiver, filter);
        }
            else {
            // if it is registered then start the next activity
            Intent myIntent = new Intent(MainActivity.this, PopulistoContactList.class);
            myIntent.putExtra("keyName", phoneNoCheck);
            MainActivity.this.startActivity(myIntent);


        }


    }



    protected void sendSMSMessage() {
        phoneNo = txtphoneNo.getText().toString();

        //this is the SMS received
        String message = "Verification test code. Please ignore this message. Thank you.";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();

            //if originating phone number is the same as the sent to number, save
            //and go to the next activity
            if (origNumber.equals(phoneNo)) {
                //save the phone number
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("phonenumber", phoneNo);
                editor.commit();

                Intent myIntent = new Intent(MainActivity.this, PopulistoContactList.class);
                MainActivity.this.startActivity(myIntent);
            }
        }

        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
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