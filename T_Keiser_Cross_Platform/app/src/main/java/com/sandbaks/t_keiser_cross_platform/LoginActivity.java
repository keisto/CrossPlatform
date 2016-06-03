package com.sandbaks.t_keiser_cross_platform;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    public static final String FIREBASE = "https://sandbaks.firebaseio.com/";
    public static final String FIREBASE_USERS = "https://sandbaks.firebaseio.com/users/";
    // Global Variables
    String authO;
    String emailString;
    String passString;
    Firebase mFirebase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Welcome");
        if (readData()!=null){
            // User has been here before
            // Move to User Area
            Intent i = new Intent(getApplicationContext(), UserAreaActivity.class);
            startActivity(i);
            finish();
        }

        Firebase.setAndroidContext(this);
        mFirebase = new Firebase(FIREBASE);

        // Attempt Sign Up to Database
        final Firebase.AuthResultHandler signUpHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Save User Locally
                saveData(emailString, passString);
                // Successful Login
                Map<String, String> map = new HashMap<String, String>();
                map.put("provider", authData.getProvider());
                map.put("email", emailString);
                if(authData.getProviderData().containsKey("displayName")) {
                    map.put("displayName", authData.getProviderData().get("displayName").toString());
                }
                // Add User to Database
                mFirebase.child("users").child(authData.getUid()).setValue(map);
                Toast.makeText(getApplicationContext(), "Login Successful!",
                        Toast.LENGTH_SHORT).show();

                // Move to User Area
                Intent i = new Intent(getApplicationContext(), UserAreaActivity.class);
                startActivity(i);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                setTitle("Sign Up Failed");
            }
        };

        // Attempt Sign Up to Database
        final Firebase.AuthResultHandler loginHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Save User Locally
                saveData(emailString, passString);
                Toast.makeText(getApplicationContext(), "Login Successful!",
                        Toast.LENGTH_SHORT).show();

                // Move to User Area
                Intent i = new Intent(getApplicationContext(), UserAreaActivity.class);
                startActivity(i);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                setTitle("Login Failed");
            }
        };

        final EditText emailText = (EditText) findViewById(R.id.email);
        final EditText passText = (EditText) findViewById(R.id.password);
        Button login = (Button) findViewById(R.id.login);
        Button signup = (Button) findViewById(R.id.signup);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailString = emailText.getText().toString();
                passString = passText.getText().toString();
                if (emailString.trim().equals("") || passString.trim().equals("")) {
                    // Empty Field(s)
                    Toast.makeText(getApplicationContext(), "Please Enter Email & Password.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    setTitle("Please Wait...");
                    // Attempt Login
                    mFirebase.authWithPassword(emailString, passString, loginHandler);
                }
            }
        });


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailString = emailText.getText().toString();
                passString = passText.getText().toString();

                if (emailString.trim().equals("") || passString.trim().equals("")) {
                    // Empty Field(s)
                    Toast.makeText(getApplicationContext(), "Please Enter Email & Password.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Attempt Login
                    mFirebase.createUser(emailString, passString, new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            setTitle("Please Wait...");
                            // User Created!
                            Toast.makeText(getApplicationContext(), "Sign Up Success!",
                                    Toast.LENGTH_LONG).show();
                            // Attempt Login
                            mFirebase.authWithPassword(emailString, passString, signUpHandler);
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            // User Create Failed!
                            Toast.makeText(getApplicationContext(), firebaseError.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    // Save User Info To Local Storage
    private Boolean saveData(String email, String pass){
        JSONObject object = new JSONObject();
        try {
            object.put("email", email);
            object.put("password", pass);
            File external = getExternalFilesDir(null);
            File file = new File(external, "user.txt");
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(object.toString());
            osw.flush();
            osw.close();
            return true;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get User Info From Local Storage
    private JSONObject readData(){
        String result = "";
        File external = this.getExternalFilesDir(null);
        File file = new File(external, "user.txt");
        if (file.exists()) {
            try {
                FileInputStream fin = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fin);
                char[] data = new char[2048];
                int size;
                try {
                    while ((size = isr.read(data)) > 0) {
                        String readData = String.copyValueOf(data, 0, size);
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
        } else {
            return null;
        }
    }
}
