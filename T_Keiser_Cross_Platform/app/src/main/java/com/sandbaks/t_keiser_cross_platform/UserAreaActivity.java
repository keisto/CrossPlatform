package com.sandbaks.t_keiser_cross_platform;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserAreaActivity extends AppCompatActivity {
    public static final String FIREBASE = "https://sandbaks.firebaseio.com/";
    public static final String FIREBASE_USERS = "https://sandbaks.firebaseio.com/users/";
    Firebase mFirebase;
    Firebase mFirebaseUser;
    String firstname = "";
    String lastname = "";
    int    age = 0;
    JSONObject object;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        mFirebase = new Firebase(FIREBASE);

        if (readData() != null) {
            object = readData();
            if (object != null) {
                // User is there! Notify Loading
                setTitle("Loading...");
                if (isConnected()) {
                    getUserInfo();
                } else {
                    // Not Connected
                    setTitle("Can't Connect");
                }
            }
        } else {
            // No User Found
            Toast.makeText(getApplicationContext(), "User Not Found!",
                    Toast.LENGTH_SHORT).show();
            // Return to Login
            if (readData()!=null) {
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
    }

    public void getUserInfo() {
        if (readData() != null) {
            object = readData();
            if (object != null) {
                try {
                    if (mFirebase.getAuth().getUid() != null ||
                            mFirebaseUser.getAuth().getUid() != null) {
                        mFirebase.authWithPassword(object.getString("email"),
                                object.getString("password"), new Firebase.AuthResultHandler() {
                                    @Override
                                    public void onAuthenticated(AuthData authData) {
                                        // Success
                                        mFirebaseUser = new Firebase(FIREBASE_USERS + authData.getUid());
                                        mFirebaseUser.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                // Set Layout
                                                setContentView(R.layout.activity_self);
                                                // Set Variables
                                                final TextView tFirstname = (TextView)
                                                        findViewById(R.id.firstname);
                                                final TextView tLastname = (TextView)
                                                        findViewById(R.id.lastname);
                                                final TextView tAge = (TextView)
                                                        findViewById(R.id.age);
                                                final TextView tEmail = (TextView)
                                                        findViewById(R.id.email);
                                                // Get User
                                                User user = snapshot.getValue(User.class);
                                                if (user != null) {
                                                    firstname = user.getFirstname();
                                                    lastname = user.getLastname();
                                                    age = user.getAge();

                                                    if (firstname == null || lastname == null) {
                                                        // If Values Not There...
                                                        setTitle("User Info");
                                                        Toast.makeText(getApplicationContext(),
                                                                "Please Edit to Enter Values.",
                                                                Toast.LENGTH_LONG).show();
                                                    } else {
                                                        // Set Text Values
                                                        tFirstname.setText(firstname);
                                                        tLastname.setText(lastname);
                                                        tAge.setText(String.valueOf(age));
                                                        tEmail.setText(user.getEmail());
                                                        // Get Last Initial
                                                        String lastInitial = lastname.substring(0, 1)
                                                                .toUpperCase();
                                                        // Set Title
                                                        setTitle("Hello " + firstname + " "
                                                                + " " + lastInitial + ",");
                                                        // Cache
                                                        mFirebaseUser.keepSynced(true);
                                                        startCheck();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {
                                                System.out.println("Read Failed: " +
                                                        firebaseError.getMessage());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onAuthenticationError(FirebaseError firebaseError) {
                                        // Failed
//                                        Toast.makeText(getApplicationContext(), "User Not Vaild!",
//                                                Toast.LENGTH_SHORT).show();
//                                        // Return to Login
//                                        if (readData() != null) {
//                                            // Delete User Saved Login
//                                            File external = getApplicationContext()
//                                                    .getExternalFilesDir(null);
//                                            File file = new File(external, "user.txt");
//                                            if (file.exists()) {
//                                                file.delete();
//                                            }
//                                            // Go To Login
//                                            Intent i = new Intent(getApplicationContext(),
//                                                    LoginActivity.class);
//                                            startActivity(i);
//                                            finish();
//                                        }
                                    }
                                });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (mFirebaseUser!=null) {
            if (id == R.id.delete) {
                if (isConnected()) {
                    // Delete Data Alert
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Delete User Information");
                    builder.setMessage("Are you sure?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete Data
                            mFirebaseUser.child("firstname").removeValue();
                            mFirebaseUser.child("lastname").removeValue();
                            mFirebaseUser.child("age").removeValue();
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return true;
                }
            }
            if (id == R.id.edit) {
                // Edit Data
                if (isConnected()) {
                    Intent i = new Intent(getApplicationContext(), UserEditActivity.class);
                    i.putExtra("first", firstname);
                    i.putExtra("last", lastname);
                    i.putExtra("age", age);
                    startActivity(i);
                    return true;
                }
            }
            if (id == R.id.logout) {
                // Logout User
                mFirebaseUser.unauth();
                mFirebase.unauth();
                if (readData()!=null) {
                    // Delete User Saved Login
                    File external = this.getExternalFilesDir(null);
                    File file = new File(external, "user.txt");
                    if (file.exists()) {
                        file.delete();
                        // Go To Login
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }


    // Update Timer 20 Seconds -> Update User Data
    Handler mHandler = new Handler();
    Runnable checkNetwork = new Runnable() {
        @Override
        public void run() {
            refreshData();
            mHandler.postDelayed(checkNetwork, 20000);
        }
    };

    public void startCheck() {
        checkNetwork.run();
    }
    public void stopCheck() {
        mHandler.removeCallbacks(checkNetwork);
    }

    // Update Timer 20 Seconds -> Update User Data
    public void refreshData() {
        if (isConnected()) {
            if (readData()!=null) {
                getUserInfo();
            }
        }
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
    private JSONObject readData() {
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
