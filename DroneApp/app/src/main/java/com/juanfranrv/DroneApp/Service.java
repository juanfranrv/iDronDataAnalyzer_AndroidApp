package com.juanfranrv.DroneApp;

/**
 * Created by juanfranrv on 6/04/16.
 */
public class Service {

    private static String token;

    public static String getToken(){
        return token;
    }

    public static void setToken(String tok){
        token = tok;
    }
}
