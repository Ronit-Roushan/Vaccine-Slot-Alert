package com.example.vaccinetracker;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private String pincode_val, date_val;
    private EditText pincode,date;
    private TextView message , status;
    private JSONArray raw;
    private RadioGroup rg_vaccine,rg_dose,rg_age,rg_price;
    private String vaccineType, doseType, priceType;
    private int ageType;
    private int countSearch = 1;
    private ProgressBar progressBar;
    private boolean slotNotAvail = false;
    private boolean agePresent = false;
    private MediaPlayer available_;
    private Vibrator vibrator;
    private int button_click_count = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button = findViewById(R.id.button);
        pincode = findViewById(R.id.editText);
        date = findViewById(R.id.editTextDate2);
        message = findViewById(R.id.textView7);
        rg_vaccine = findViewById(R.id.radioGroup);
        rg_dose = findViewById(R.id.radioGroup2);
        rg_age = findViewById(R.id.radioGroup3);
        rg_price = findViewById(R.id.radioGroup4);
        status = findViewById(R.id.textView8);
        progressBar = findViewById(R.id.progressBar);

        Log.d("STAGE", "id set");
        progressBar.setVisibility(View.GONE);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareUrl = "https://drive.google.com/file/d/1c_5f64wQWQLUZkfyz_KvqqfYMozyZ6-Y/view?usp=sharing";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(intent.EXTRA_TEXT, "Hey checkout this very usefull vaccine slot alert application: "+shareUrl);
//                intent.putExtra(Intent.EXTRA_STREAM, attachment);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        available_ = MediaPlayer.create(this,R.raw.music);
        vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countSearch = 1;
                if(button_click_count > 1){
                    if(available_.isPlaying()){
                        available_.pause();
                        vibrator.cancel();
                    }
                }
                if(pincode.getText().toString().trim().length() != 6){
                    Toast.makeText(MainActivity.this, "Enter PinCode Correctly", Toast.LENGTH_SHORT).show();
                }else if(date.getText().toString().trim().length() != 10){
                    Toast.makeText(MainActivity.this, "Enter Date Correctly", Toast.LENGTH_SHORT).show();
                }else {
                    Log.d("STAGE", "Button clicked");
                    pincode_val = pincode.getText().toString().trim();
                    Log.d("STAGE", "pincode got");
                    date_val = date.getText().toString().trim();
                    Log.d("STAGE", "date got");
                    message.setText("");
                    if(check()){
                        message.setText("Command Started Keep your Internet connection turned on...\n and also keep app open in recent tab...");
                        status.setText("SEARCHING>>>" + countSearch);
                        countSearch++;
                        progressBar.setVisibility(View.VISIBLE);
                        search();
                        Log.d("STAGE", "search called"+ countSearch);
                    }
                }
                button_click_count++;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_search) {
            available_.stop();
            finish();
            return true;
        }
        else if(id == R.id.review){
            String [] mailId = {"inforonit88@gmail.com"};
            String subject = "Feedback/Bug-(Vaccine Alert)";
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, mailId);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void search(){

        String url = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByPin?pincode="+pincode_val+"&date="+date_val;
        Log.d("STAGE", "string setting over");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("STAGE", "response got");
                        try {
                            Log.d("STAGE", "into try");
                            raw = response.getJSONArray("sessions");
                            if(raw.length()==0){
                                Log.d("STAGE", "seaching because of array lenght 0 - " + countSearch);
                                progressBar.setVisibility(View.VISIBLE);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        status.setText("SEARCHING>>>" + countSearch);
                                        countSearch+=1;
                                        search();
                                    }
                                },5000);
                            }
                            else{
                                for(int i = 0; i<raw.length(); i++){
                                    int temp = raw.getJSONObject(i).getInt("min_age_limit");
                                    if(temp == ageType){
                                        agePresent = true;
                                        Log.d("STAGE", "age is present");
                                        break;
                                    }
                                }

                                if(agePresent){
                                    Log.d("STAGE", "age present loop started");
                                    for(int i = 0; i<raw.length(); i++){
                                        int temp = raw.getJSONObject(i).getInt("available_capacity_"+doseType);
                                        if(temp>0){
                                            Log.d("STAGE", "temp>0 section");
                                            String vac = raw.getJSONObject(i).getString("vaccine").trim();
                                            String pri = raw.getJSONObject(i).getString("fee_type").trim();
                                            if(vac.equals(vaccineType) && pri.equals(priceType)){
                                                Log.d("STAGE", "SLOT CONSTRAINS MATCHED");
                                                message.setText("SLOT AVAILABLE !!\n");
                                                status.setText("Search Completed...");
                                                vibrator.vibrate(30000);
                                                progressBar.setVisibility(View.GONE);
                                                available_.start();
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        message.append("If you Liked our service or have some complain then please give feedback in our feedback section..");

                                                    }
                                                },1000);
                                                slotNotAvail = false;
                                                break;
                                            }
                                            else {
                                                Log.d("STAGE", "constrains Failed");
                                            }
                                        }else {
                                            Log.d("STAGE", "Slot not available = true");
                                            slotNotAvail = true;
                                        }
                                    }
                                }
                                else {
                                    Log.d("STAGE", "age not present search");

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            status.setText("SEARCHING>>>" + countSearch);
                                            countSearch++;
                                            search();
                                        }
                                    },5000);
                                }

                                if(slotNotAvail == true && agePresent == true){
                                    message.setText("Vaccine Has Been Booked On This Day !!! \n But Don't Worry We will keep searching for droped Slots Or New Slots For You...\n Or Made A New Command For Next Date...");
                                    status.setText("SEARCHING>>>" + countSearch);
                                    Log.d("STAGE", "seaching again - " + countSearch);
                                    progressBar.setVisibility(View.VISIBLE);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            search();
                                            status.setText("SEARCHING>>>" + countSearch);
                                            countSearch++;
                                        }
                                    },5000);

                                }
                            }
                            Log.d("STAGE", "raw and message set");
                        } catch (JSONException e) {
                            Log.d("STAGE", "got catch");
                            Toast.makeText(MainActivity.this, "we faced some error--> make sure fields are filled correctly --> if they are correct then chill we are continuing our search.... ", Toast.LENGTH_SHORT).show();
                             new Handler().postDelayed(new Runnable() {
                             @Override
                             public void run() {
                                search();
                                status.setText("SEARCHING>>>" + countSearch);
                                countSearch++;
                                }
                                },5000);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("STAGE", "response error");
                        Toast.makeText(MainActivity.this, "we faced some error--> make sure fields are filled correctly --> if they are correct then chill we are continuing our search.... ", Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(new Runnable() {
                          @Override
                          public void run() {
                            search();
                            status.setText("SEARCHING>>>" + countSearch);
                            countSearch++; }
                          },5000);

                    }
                });

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        Log.d("STAGE", "search over");
    }

    public boolean check() {
        boolean reponse = true;
        if((rg_vaccine.getCheckedRadioButtonId()) == -1){
            Toast.makeText(this, "Select Vaccine", Toast.LENGTH_SHORT).show();
            reponse = false;
        }else {
            if(rg_vaccine.getCheckedRadioButtonId() == R.id.COVAXIN){
                vaccineType = "COVAXIN";
            }
            else{
                vaccineType = "COVISHIELD";
            }
        }
        if((rg_dose.getCheckedRadioButtonId()) == -1){
            Toast.makeText(this, "Select Dose", Toast.LENGTH_SHORT).show();
            reponse = false;
        }else {
            if(rg_dose.getCheckedRadioButtonId() == R.id.dose1){
                doseType = "dose1";
            }
            else{
                doseType = "dose2";
            }
        }
        if((rg_age.getCheckedRadioButtonId()) == -1){
            Toast.makeText(this, "Select Age", Toast.LENGTH_SHORT).show();
            reponse = false;
        }else {
            if(rg_age.getCheckedRadioButtonId() == R.id.option18){
                ageType = 18;
            }
            else{
                ageType = 45;
            }
        }
        if((rg_price.getCheckedRadioButtonId()) == -1){
            Toast.makeText(this, "Select Type", Toast.LENGTH_SHORT).show();
            reponse = false;
        }else {
            if(rg_price.getCheckedRadioButtonId() == R.id.Free){
                priceType = "Free";
            }
            else{
                priceType = "Paid";
            }
        }
        return reponse;
    }

}