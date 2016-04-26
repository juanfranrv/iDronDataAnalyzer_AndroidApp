package com.juanfranrv.DroneApp;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by juanfranrv on 26/04/16.
 */

//Singleton para crear sólo una cola de peticiones en toda la aplicación

public class VolleyRequestQueue {

    private static VolleyRequestQueue mVolleyS = null;
    //Este objeto es la cola que usará la aplicación
    private RequestQueue mRequestQueue;

    private VolleyRequestQueue(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static VolleyRequestQueue getInstance(Context context) {
        if (mVolleyS == null) {
            mVolleyS = new VolleyRequestQueue(context);
        }
        return mVolleyS;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
