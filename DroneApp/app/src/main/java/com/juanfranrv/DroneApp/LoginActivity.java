package com.juanfranrv.DroneApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener  {

    public static final String LOGIN_URL = "http://idrondataanalyzer.appspot.com/recibirDatosLoginApp";

    public static final String KEY_USERNAME="username";
    public static final String KEY_PASSWORD="password";

    private EditText editTextUsername;
    private EditText editTextPassword;
    private FancyButton buttonLogin;

    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        buttonLogin = (FancyButton) findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(this);

        SharedPreferences preferences = getSharedPreferences("temp", getApplicationContext().MODE_PRIVATE);
        String login = preferences.getString("token","");

        if(login != ""){
            Intent i = new Intent(this, MainActivity.class);
            Service.setToken(login);
            startActivity(i);
            finish();
        }
    }

    private void userLogin() {
        username = editTextUsername.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                         if (response.trim().equals("NoData")) {
                             Toast.makeText(LoginActivity.this,"Wrong username or password",Toast.LENGTH_LONG).show();
                        }else{
                             Service.setToken(response);

                             SharedPreferences preferences = getSharedPreferences("temp", getApplicationContext().MODE_PRIVATE);
                             SharedPreferences.Editor editor = preferences.edit();
                             editor.putString("token", Service.getToken());
                             editor.commit();

                             openProfile();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this,error.toString(),Toast.LENGTH_LONG ).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();
                map.put(KEY_USERNAME,username);
                map.put(KEY_PASSWORD,password);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void openProfile(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(KEY_USERNAME, username);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        userLogin();
    }
}
