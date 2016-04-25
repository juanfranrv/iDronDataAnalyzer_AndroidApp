package com.juanfranrv.DroneApp;

/**
 * Created by juanfranrv on 22/03/16.
 */

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class httpHandler {

    private MyAsyncTask mTask = new MyAsyncTask();

    public void postData(String posturl, String token, String latitude, String longitude, String altitude, String speed){
        mTask.execute(posturl, token, latitude, longitude, altitude, speed);

    }

    public void stopPostData(){
        if(mTask != null) {
            mTask.cancel(true);
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Double> {

        @Override
        protected Double doInBackground(String... params) {
            post(params[0], params[1], params[2], params[3], params[4], params[5]);
            return null;
        }

        public void post(String posturl, String token, String latitude, String longitude, String altitude, String speed){
            try {

                HttpClient httpclient = new DefaultHttpClient();
                //Creamos el objeto de HttpClient que nos permitira conectarnos mediante peticiones http y HttpPost que nos permite enviar peticiones POST a una url determinada
                HttpPost httppost = new HttpPost(posturl);

                //El objeto HttpPost permite que enviemos una peticion de tipo POST a una URL especificada
                //AÑADIR PARAMETROS
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("token", token));
                params.add(new BasicNameValuePair("latitud", latitude));
                params.add(new BasicNameValuePair("longitud", longitude));
                params.add(new BasicNameValuePair("altura", altitude));
                params.add(new BasicNameValuePair("velocidad", speed));

                httppost.setEntity(new UrlEncodedFormEntity(params));
                //Enviamos la info al server
                httpclient.execute(httppost);

            }

            catch(Exception e) {
                e.printStackTrace();
                System.out.println("Not working");
            }

        }
    }
}
