package com.juanfranrv.DroneApp;

import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by juanfranrv on 26/04/16.
 */

public class VolleyHttpPost{

    private VolleyRequestQueue volley;
    protected RequestQueue fRequestQueue;

    public void postData(final Context context, String posturl, final String token, final String latitude, final String longitude, final String altitude, final String speed) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, posturl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       // Toast.makeText(VolleyHttpPost.this,"Error",Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,error.toString(),Toast.LENGTH_LONG ).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();
                map.put("token", token);
                map.put("latitud", latitude);
                map.put("longitud", longitude);
                map.put("altura", altitude);
                map.put("velocidad", speed);

                return map;
            }
        };

        //Llamada al singleton para crear una única cola en la que almacenar toda la información
        volley = VolleyRequestQueue.getInstance(context);
        fRequestQueue = volley.getRequestQueue();

        fRequestQueue.add(stringRequest);
    }

}
