package com.sandbaks.t_keiser_cross_platform;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import java.net.MalformedURLException;
import java.net.URL;

public class UserAreaActivity extends AppCompatActivity {
    Firebase mFirebase;
    Firebase mFirebaseUser;
    String firstname = "";
    String lastname = "";
    int    age = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        mFirebase = new Firebase(LoginActivity.FIREBASE);

        if (readData() != null) {
            JSONObject object = readData();
            try {
                if (object != null) {
                    // User is there! Notify Loading
                    setTitle("Loading...");
                    mFirebase.authWithPassword(object.getString("email"),
                            object.getString("password"), new Firebase.AuthResultHandler() {
                                @Override
                                public void onAuthenticated(AuthData authData) {
                                    // Success
                                    mFirebaseUser = new Firebase(LoginActivity
                                            .FIREBASE_USERS + authData.getUid());
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
                                                lastname  = user.getLastname();
                                                age       = user.getAge();

                                                if (firstname==null||lastname==null) {
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
                                    Toast.makeText(getApplicationContext(), "User Not Vaild!",
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
                            });
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
            if (id == R.id.edit) {
                // Edit Data
                Intent i = new Intent(getApplicationContext(), UserEditActivity.class);
                i.putExtra("first", firstname);
                i.putExtra("last",  lastname);
                i.putExtra("age",   age);
                startActivity(i);
                return true;
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
