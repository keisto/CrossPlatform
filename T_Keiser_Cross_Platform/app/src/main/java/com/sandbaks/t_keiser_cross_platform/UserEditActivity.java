package com.sandbaks.t_keiser_cross_platform;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserEditActivity extends AppCompatActivity {
    Firebase mFirebase;
    EditText eFirstname;
    EditText eLastname;
    EditText eAge;
    Button bSave;
    Button bCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setTitle("Edit Info");
        Firebase.setAndroidContext(this);
        mFirebase = new Firebase(LoginActivity.FIREBASE);
        // Get User
        JSONObject u = readData();
        if (isConnected()) {
            // Check User
            try {
                mFirebase.authWithPassword(u.getString("email"), u.getString("password"),
                        new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(AuthData authData) {
                                // Success
                            }

                            @Override
                            public void onAuthenticationError(FirebaseError firebaseError) {
                                // Failed
                                Toast.makeText(getApplicationContext(), "User Not Vaild!",
                                        Toast.LENGTH_SHORT).show();
                                // Return to Login
                                if (readData() != null) {
                                    // Delete User Saved Login
                                    File external = getApplicationContext()
                                            .getExternalFilesDir(null);
                                    File file = new File(external, "user.txt");
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    // Go To Login
                                    Intent i = new Intent(getApplicationContext(),
                                            LoginActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        eFirstname = (EditText) findViewById(R.id.firstname);
        eLastname  = (EditText) findViewById(R.id.lastname);
        eAge       = (EditText) findViewById(R.id.age);
        bSave      = (Button)   findViewById(R.id.save);
        bCancel    = (Button)   findViewById(R.id.cancel);

        // If Editing Existing... Should have Extras
        if (getIntent().hasExtra("first")) {
            eFirstname.setText(getIntent().getExtras().getString("first"));
            eLastname.setText(getIntent().getExtras().getString("last"));
            eAge.setText(String.valueOf(getIntent().getExtras().getInt("age")));
        }

        //On Save Click
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    // Check Values
                    String firstname = eFirstname.getText().toString().trim();
                    String lastname = eLastname.getText().toString().trim();
                    int age;
                    if (eAge.getText().toString().trim().equals("")) {
                        age = 0;
                    } else {
                        age = Integer.parseInt(eAge.getText().toString().trim());
                    }
                    if (firstname.equals("") || lastname.equals("") || age == 0) {
                        // Inputs Not Valid
                        Toast.makeText(getApplicationContext(), "Please Enter All Fields.",
                                Toast.LENGTH_LONG).show();
                    } else if (firstname.length() > 32 || lastname.length() > 32) {
                        // Check firstname && lastname
                        Toast.makeText(getApplicationContext(),
                                "Please shorten the name under 32 characters.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        // Try Enter Inputs to Database
                        AuthData autho = mFirebase.getAuth();
                        if (autho != null) {
                            // Success
                            mFirebase.child("users").child(autho.getUid())
                                    .child("firstname").setValue(firstname);
                            mFirebase.child("users").child(autho.getUid())
                                    .child("lastname").setValue(lastname);
                            mFirebase.child("users").child(autho.getUid())
                                    .child("age").setValue(age);
                            finish();
                        } else {
                            // Failed
                            Toast.makeText(getApplicationContext(), "Update Failed.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

        //On Cancel Click
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Check Internet Connection
    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null) {
            if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            } else if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        } else {
            Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_LONG).show();
            return false;
        }
        Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_LONG).show();
        return false;
    }

    // Get User Info From Local Storage
    private JSONObject readData(){
        String result = "";
        File external = this.getExternalFilesDir(null);
        File file = new File(external, "user.txt");
        try {
            FileInputStream fin= new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fin);
            char[] data = new char[2048];
            int size;
            try {
                while ((size = isr.read(data))>0){
                    String readData = String.copyValueOf(data,0,size);
                    result += readData;
                    data = new char[2048];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JSONObject object = null;
        try {
            object = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
}
