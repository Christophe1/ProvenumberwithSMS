package com.example.chris.tutorialspoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    EditText phoneNoCheck;

    String phoneNo;
    String origNumber;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendBtn = (Button) findViewById(R.id.btnSendSMS);
        txtphoneNo = (EditText) findViewById(R.id.editText);
        phoneNoCheck = (EditText) findViewById(R.id.editText);

//      when the form loads, check Edittext2 to see if phoneNo is in there
        //  if it is in there, proceed to PopulistoContactList activity

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

                System.out.println(origNumber);
//                System.out.println("test test");
                if(phoneNo != null) {
                    System.out.println(phoneNo);
                }

                Toast.makeText(getApplicationContext(), "Originating number" + origNumber, Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "Sent to number" + phoneNo, Toast.LENGTH_LONG).show();


                if (origNumber.equals(phoneNo)) {

                    Toast.makeText(getApplicationContext(), "Correct, they're the same.!", Toast.LENGTH_LONG).show();
                    //start the new Activity
                    Intent myIntent = new Intent(MainActivity.this, PopulistoContactList.class);
                    MainActivity.this.startActivity(myIntent);
                }
            }

        };
        registerReceiver(receiver, filter);
    }



    protected void sendSMSMessage() {
        Log.i("Send SMS", "");
        phoneNo = txtphoneNo.getText().toString();
        String message = "Verification test code. Please ignore this message. Thank you.";
//        String message = txtMessage.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
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