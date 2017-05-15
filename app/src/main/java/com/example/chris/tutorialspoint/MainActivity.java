package com.example.chris.tutorialspoint;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.provider.ContactsContract;
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

import com.android.volley.AuthFailureError;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity  {

    // this is the php file name where to insert into the database, the user's phone number
    private static final String REGISTER_URL = "http://www.populisto.com/insert.php";

    //we are posting phoneNoofUser, which in PHP is phonenumberofuser
    public static final String KEY_PHONENUMBER_USER = "phonenumberofuser";

    //*************TO DO WITH COMPARING APP CONTACTS AND PHONE CONTACTS*******************

    // this is the php file we are contacting with Volley to see what contacts are using the App
    private static final String CHECKPHONENUMBER_URL = "http://www.populisto.com/checkcontact.php";

    //we are posting phoneNoofContact, which in PHP is phonenumberofcontact

    public static final String KEY_PHONENUMBER_CONTACT = "phonenumberofcontact";

    //alContacts is a list of all the phone numbers in the user's contacts
    public static final ArrayList<String> alContacts = new ArrayList<String>();

    // JSONObject dataToSend = new JSONObject();
    JSONArray jsonArrayContacts = new JSONArray();

    Cursor cursor;
    String name;
    String phoneNumberofContact;
    String lookupkey;

    //*************************************************************************************

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

        //  when the form loads, check to see if phoneNoofUser is in there,if the user is
        // already registered, by checking the MyData XML file
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String phoneNoofUserCheck = sharedPreferences.getString("phonenumberofuser", "");

        //  if the user has not already registered
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

        //Here just for testing purposes
  /*      buttonRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                System.out.println("you clicked it, register");
                phoneNoofUser = txtphoneNoofUser.getText().toString();
                registerUser();
            }
        });*/

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
                    registerUser();
                    //get all the contacts on the user's phone
                    getPhoneContacts();
                    //convert all contacts on the user's phone to JSON
                    convertNumberstoJSON();

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

 /*   @Override
    public void onClick(View v) {
        phoneNoofUser = txtphoneNoofUser.getText().toString();
        if(v== buttonRegister){
            System.out.println("you clicked it");
            registerUser();
        }
    }*/

    // register the user's phone number in the user table, this is called
    //when the phone number is verified
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
                params.put(KEY_PHONENUMBER_USER, phoneNoofUser);
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


    //get the names and phone numbers of all contacts in phone book, take out duplicates
    protected void getPhoneContacts() {
//          we have this here to avoid cursor errors
        if (cursor != null) {
            cursor.moveToFirst();

        }


        try {

//                get a handle on the Content Resolver, so we can query the provider,
            cursor = getApplicationContext().getContentResolver()
//                the table to query
                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//               Null. This means that we are not making any conditional query into the contacts table.
//               Hence, all data is returned into the cursor.
//                                Projection - the columns you want to query
                            null,
//                                Selection - with this you are extracting records with assigned (by you) conditions and rules
                            null,
//                                SelectionArgs - This replaces any question marks (?) in the selection string
//                               if you have something like String[] args = { "first string", "second@string.com" };
                            null,
//                                display in ascending order
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

//                get the column number of the Contact_ID column, make it an integer.
//                I think having it stored as a number makes for faster operations later on.
            int Idx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
//                get the column number of the DISPLAY_NAME column
            int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
//                 get the column number of the NUMBER column
            int phoneNumberofContactIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

//                ****
            int contactlookupkey = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY);
//                ****
//                cursor.moveToFirst();
//        String contactlookupkey2 = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY));


//                int photoIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI);


            cursor.moveToFirst();

//              We make a new Hashset to hold all our contact_ids, including duplicates, if they come up
            Set<String> ids = new HashSet<>();
            do {
                System.out.println("=====>in while");
//                  get a handle on the contactid, which is a string. Loop through all the contact_ids
                String contactid = cursor.getString(Idx);
//                  if our Hashset doesn't already contain the contactid string,
//                    then add it to the hashset
                if (!ids.contains(contactid)) {
                    ids.add(contactid);

                    HashMap<String, String> hashMap = new HashMap<String, String>();
//                        get a handle on the display name, which is a string
                    name = cursor.getString(nameIdx);
//                        get a handle on the phone number, which is a string
                    phoneNumberofContact = cursor.getString(phoneNumberofContactIdx);
//                        String image = cursor.getString(photoIdIdx);


                    //------------------------------------------------------
                    //replace numbers starting with 00 with +
                    //if (phoneNumberofContact.startsWith("00")) {
                    //    System.out.println(phoneNumberofContact = phoneNumberofContact.replaceFirst("00", "+"));
                    //}
                    // remove splaces between phone numbers
                    //phoneNumberofContact = phoneNumberofContact.replaceAll("\\s+", "");

                    //all phone numbers not starting with +, make them E.164 format,
                    //for Irish phones. Although it should really be for the country code of the User
                    // if (!phoneNumberofContact.startsWith("+")) {


                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    try {
                        Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumberofContact, "IE");
                        phoneNumberofContact = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
                        //Since you know the country you can format it as follows:
                        //System.out.println(phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164));
                    } catch (NumberParseException e) {
                        System.err.println("NumberParseException was thrown: " + e.toString());
                    }
                    // }

                    //----------------------------------------------------------

                    alContacts.add(phoneNumberofContact);

                    lookupkey = cursor.getString(contactlookupkey);

//                    System.out.println("Id--->"+contactid+"Name--->"+name);
                    System.out.println("Id--->" + contactid + " Name--->" + name);
                    System.out.println("Id--->" + contactid + " Numberbob--->" + phoneNumberofContact);
                    System.out.println("Id--->" + contactid + " lookupkey--->" + lookupkey);
//                        System.out.println("Id--->" + contactid + " lookupkey2--->" + contactlookupkey2);

//                        if (!phoneNumberofContact.contains("*")) {
//                            hashMap.put("contactid", "" + contactid);
//                            hashMap.put("name", "" + name);
//                            hashMap.put("phoneNumberofContact", "" + phoneNumberofContact);
//                            hashMap.put("image", "" + image);
                    // hashMap.put("email", ""+email);
//                            if (hashMapsArrayList != null) {
//                                hashMapsArrayList.add(hashMap);
//                            }
//                    hashMapsArrayList.add(hashMap);
//                        }

                    // SelectContact selectContact = new SelectContact();
//                    selectContact.setThumb(bit_thumb);
                    // selectContact.setName(name);
                    // selectContact.setPhone(phoneNumberofContact);
                    // selectContact.setLookup(lookupkey);
//                    selectContact.setCheckedBox(false);
                    // selectContacts.add(selectContact);
                }


            } while (cursor.moveToNext());


        } catch (Exception e) {
            e.printStackTrace();
            cursor.close();
        } finally {
//                if (cursor != null) {
            cursor.close();
//                }
        }
        ;
// cursor.close();
//return null;
    }


    protected void convertNumberstoJSON() {

        try {
            //  JSONObject dataToSend = new JSONObject();

            // contacts
            //  JSONArray jsonArrayContacts = new JSONArray();
            //alContacts is our arraylist with all the phone numbers
            for (int i = 0; i < alContacts.size(); i++)
            {
                // make each contact in alContacts into an individual JSON object called jsonObjectContact
                JSONObject jsonObjectContact = new JSONObject();
                // jsonObjectContact will be of the form {"phone_number":"123456789"}
                jsonObjectContact.put("phone_number", alContacts.get(i));

                // Add jsonObjectContact to contacts jsonArray
                jsonArrayContacts.put(jsonObjectContact);

            }
            System.out.println("the amount in alContacts :" + alContacts.size());
            // Add contacts jsonArray to jsonObject dataToSend
            // dataToSend.put("contacts", jsonArrayContacts);

            System.out.println("JSONarraycontacts: " + jsonArrayContacts.toString());
            //System.out.println("JSON object datatoSend: " + dataToSend.toString());


        } catch (final JSONException e) {
            Log.e("FAILED", "Json parsing error: " + e.getMessage());
        }

        CheckifUserisContact();
    }



    private void CheckifUserisContact() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECKPHONENUMBER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                        //textView.append(response + " \n");

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();

                    }

                }) {



            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //The KEY, KEY_PHONENUMBER = "phonenumber" . In PHP we will have $_POST["phonenumber"]
                //The VALUE, phonenumber, will be of the form "12345678"
                params.put(KEY_PHONENUMBER_CONTACT,jsonArrayContacts.toString());
                System.out.println(Collections.singletonList(params));
                //System.out.println("contact is : " + jsonArrayContacts);
                return params;


            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }


}