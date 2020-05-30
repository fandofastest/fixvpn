package com.lazycoder.cakevpn.view;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lazycoder.cakevpn.R;
import com.lazycoder.cakevpn.model.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "Activity";
    private static final int REQUEST_SIGNUP = 0;
    private static final String DOWNLOAD_DIRECTORY = "/Config" ;
    public static String SERVERURL="https://team7.xyz/vpn/server.json";
    private static String LOGINURL="https://team7.xyz/vpn/cekuser.php?";
    SharedPreferences userPreferences;

    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login)
    Button _loginButton;
    @BindView(R.id.link_signup)
    TextView _signupLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 2909);
            } else {
            }
        } else {
        }


        userPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);




        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String username  =  _emailText.getText().toString();
                String pass=_passwordText.getText().toString();

                String LURL =LOGINURL+"user="+username+"&pass="+pass;

                login(LURL);
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String url = "https://wa.me/6281378949932";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

            }
        });




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 2909) {
            getserver(SERVERURL);
        }
    }


    public void login(String URL) {
        Log.d(TAG, "Login");
        System.out.println(URL);
        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SplashActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();


        final ProgressDialog progressDialogserver = new ProgressDialog(SplashActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialogserver.setIndeterminate(true);
        progressDialogserver.setMessage("Checking Server...");
        progressDialogserver.show();

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, URL, null, response -> {

            try {

                String status=response.getString("status");
                System.out.println("status "+status);


                if (status.equals("success")){


                    String usersave  =  _emailText.getText().toString();
                    String passsave=_passwordText.getText().toString();

                    SharedPreferences.Editor editor = userPreferences.edit();

                    editor.putString("username", usersave);
                    editor.putString("password", passsave);
                    editor.apply();

                    onLoginSuccess();

                    progressDialog.dismiss();
                    // onLoginFailed();
                }else {

                    progressDialog.dismiss();

                    _emailText.setError("Invalid Account");


                }




            } catch (JSONException e) {
                Log.e("json", "ERROR");


                e.printStackTrace();
            }





        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", String.valueOf(error));


            }
        });

        Volley.newRequestQueue(SplashActivity.this).add(jsonObjectRequest);






    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        Intent intent = new Intent(SplashActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() ){
            _emailText.setError("enter a valid user");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 1 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }


    private void getserver(String url){
        final ProgressDialog progressDialogserver = new ProgressDialog(SplashActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialogserver.setIndeterminate(true);
        progressDialogserver.setMessage("Checking Server...");
        progressDialogserver.show();

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, response -> {

            try {

                ArrayList<Server> listserver=new ArrayList<>();
                JSONArray jsonArray=response.getJSONArray("server");
                Server server =  new Server();
                File dir = new File(Environment.getExternalStorageDirectory()+DOWNLOAD_DIRECTORY);
                if (dir.isDirectory())
                {
                    String[] children = dir.list();
                    for (int i = 0; i < children.length; i++)
                    {
                        new File(dir, children[i]).delete();
                    }
                }


                for (int i=0 ;i< jsonArray.length();i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    server.setCountry(object.getString("country"));
                    long downloadFileRef = downloadFile(Uri.parse(object.getString("config")), DOWNLOAD_DIRECTORY, object.getString("country")+".ovpn");
                    if (downloadFileRef != 0) {
                        progressDialogserver.hide();
                    }else {
                        Toast.makeText(SplashActivity.this,"Please check your Internet Connection",Toast.LENGTH_LONG).show();
                    }
                    listserver.add(server);
                }

            } catch (JSONException e) {
                Log.e("json", "ERROR");


                e.printStackTrace();
            }





        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", String.valueOf(error));


            }
        });

        Volley.newRequestQueue(SplashActivity.this).add(jsonObjectRequest);

    }




    private long downloadFile(Uri uri, String fileStorageDestinationUri, String fileName) {

        long downloadReference = 0;

        DownloadManager downloadManager = (DownloadManager)SplashActivity.this.getSystemService(DOWNLOAD_SERVICE);
        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);

            //Setting title of request
            request.setTitle(fileName);

            //Setting description of request

            //set notification when download completed
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            //Set the local destination for the downloaded file to a path within the application's external files directory
            request.setDestinationInExternalPublicDir(fileStorageDestinationUri, fileName);

            request.allowScanningByMediaScanner();

            //Enqueue download and save the referenceId
            downloadReference = downloadManager.enqueue(request);
        } catch (IllegalArgumentException e) {


        }
        return downloadReference;
    }





}
