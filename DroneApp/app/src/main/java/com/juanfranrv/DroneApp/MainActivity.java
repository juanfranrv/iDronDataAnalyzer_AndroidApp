package com.juanfranrv.DroneApp;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.apis.solo.SoloCameraApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes;
import com.o3dr.services.android.lib.drone.companion.solo.SoloState;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import java.util.List;


public class MainActivity extends AppCompatActivity implements DroneListener, TowerListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String latitud, longitud, altura, velocidad;
    private Drone drone;
    private ControlTower controlTower;
    private final Handler handler = new Handler();

    private static final int DEFAULT_UDP_PORT = 14550;
    private static final int DEFAULT_USB_BAUD_RATE = 57600;

    private Button startVideoStream;
    private Button stopVideoStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        final Context context = getApplicationContext();
        this.controlTower = new ControlTower(context);
        this.drone = new Drone(context);

        //Captura la foto usando la GoPro del drone
        final Button takePic = (Button) findViewById(R.id.take_photo_button);
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        //Configura la camara GoPro del drone para comenzar el streaming
        final Button toggleVideo = (Button) findViewById(R.id.toggle_video_recording);
        toggleVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVideoRecording();
            }
        });

        final TextureView videoView = (TextureView) findViewById(R.id.video_content);
        videoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                alertUser("La reproducción de video está disponible");
                startVideoStream.setEnabled(true);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                startVideoStream.setEnabled(false);
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        //Activa el streaming
        startVideoStream = (Button) findViewById(R.id.start_video_stream);
        startVideoStream.setEnabled(false);
        startVideoStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertUser("Comenzar videostream");
                startVideoStream(new Surface(videoView.getSurfaceTexture()));
            }
        });

        //Configura la GoPro para parar el stream
        stopVideoStream = (Button) findViewById(R.id.stop_video_stream);
        stopVideoStream.setEnabled(false);
        stopVideoStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertUser("Parar videostream");
                stopVideoStream();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        this.controlTower.connect(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.drone.isConnected()) {
            this.drone.disconnect();
            updateConnectedButton(false);
        }

        this.controlTower.unregisterDrone(this.drone);
        this.controlTower.disconnect();
    }

    // 3DR Services Listener
    // ==========================================================

    @Override
    public void onTowerConnected() {
        alertUser("Servicios 3DR conectados");
        this.controlTower.registerDrone(this.drone, this.handler);
        this.drone.registerDroneListener(this);
    }

    @Override
    public void onTowerDisconnected() {
        alertUser("Servicios 3DR interrumpidos");
    }

    // Drone Listener
    // ==========================================================

    @Override
    public void onDroneEvent(String event, Bundle extras) {

        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                alertUser("Drone Conectado");
                updateConnectedButton(this.drone.isConnected());
                checkSoloState();
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                alertUser("Drone Desconectado");
                updateConnectedButton(this.drone.isConnected());
                break;

            case AttributeEvent.SPEED_UPDATED:
                updateSpeed();
                break;

            case AttributeEvent.ALTITUDE_UPDATED:
                updateAltitude();
                break;

            case AttributeEvent.GPS_POSITION:
                updateGPS();
                break;

            default:
//                Log.i("DRONE_EVENT", event); //Ver eventos del drone
                break;
        }

    }

    private void checkSoloState() {
        final SoloState soloState = drone.getAttribute(SoloAttributes.SOLO_STATE);
        if (soloState == null) {
            alertUser("Imposible comprobar el estado del solo");
        } else {
            alertUser("Estado del solo listo.");
        }
    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult result) {
        alertUser("Conexión fallida" + result.getErrorMessage());
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }

    // UI Events
    // ==========================================================

    public void onBtnConnectTap(View view) {
        if (this.drone.isConnected()) {
            this.drone.disconnect();
        } else {
            Spinner connectionSelector = (Spinner) findViewById(R.id.selectConnectionType);
            int selectedConnectionType = connectionSelector.getSelectedItemPosition();

            //Enviar datos al servidor
            httpHandler handler = new httpHandler();
            handler.post("http://idrondataanalyzer.appspot.com/recibirDatosDrone", "12", "12", "10", "10");
            //handler.post("http://idrondataanalyzer.appspot.com/recibirDatosDrone", getLatitud(), getLongitud(), getAltura(), getVelocidad());

            Bundle extraParams = new Bundle();
            if (selectedConnectionType == ConnectionType.TYPE_USB) {
                extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, DEFAULT_USB_BAUD_RATE); // Set default baud rate to 57600
            } else {
                extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, DEFAULT_UDP_PORT); // Set default baud rate to 14550
            }

            ConnectionParameter connectionParams = new ConnectionParameter(selectedConnectionType, extraParams, null);
            this.drone.connect(connectionParams);
        }
    }

    // UI updating
    // ==========================================================

    protected void updateConnectedButton(Boolean isConnected) {
        Button connectButton = (Button) findViewById(R.id.btnConnect);
        if (isConnected) {
            connectButton.setText("Desconectado");
        } else {
            connectButton.setText("Conectado");
        }
    }

    protected void updateAltitude() {
        TextView altitudeTextView = (TextView) findViewById(R.id.altitudeValueTextView);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        setAltura(String.valueOf(droneAltitude.getAltitude()));
        altitudeTextView.setText(String.format("%3.1f", droneAltitude.getAltitude()) + "m");
        //Enviar datos al servidor
       // httpHandler handler = new httpHandler();
       // handler.post("http://idrondataanalyzer.appspot.com/recibirDatosDrone", getLatitud(), getLongitud(), getAltura(), getVelocidad());
    }

    protected void updateSpeed() {
        TextView speedTextView = (TextView) findViewById(R.id.speedValueTextView);
        Speed droneSpeed = this.drone.getAttribute(AttributeType.SPEED);
        setVelocidad(String.valueOf(droneSpeed.getGroundSpeed()));
        speedTextView.setText(String.format("%3.1f", droneSpeed.getGroundSpeed()) + "m/s");
        //Enviar datos al servidor
        //httpHandler handler = new httpHandler();
        //handler.post("http://idrondataanalyzer.appspot.com/recibirDatosDrone", getLatitud(), getLongitud(), getAltura(), getVelocidad());
    }

    protected void updateGPS() {
        TextView distanceTextView = (TextView) findViewById(R.id.distanceValueTextView);
        Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        LatLong vehiclePosition = droneGps.getPosition();
        setLatitud(String.valueOf(vehiclePosition.getLatitude()));
        setLongitud(String.valueOf(vehiclePosition.getLongitude()));
        distanceTextView.setText(String.format("%.2f", vehiclePosition.getLatitude()) + " | " + String.format("%.2f", vehiclePosition.getLongitude()));
    }

    // Métodos de ayuda
    // ==========================================================

    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message);
    }

    private void takePhoto() {
        SoloCameraApi.getApi(drone).takePhoto(new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                alertUser("Foto capturada.");
            }

            @Override
            public void onError(int executionError) {
                alertUser("Error mientras se realiza la foto: " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Timeout mientras se realiza la foto");
            }
        });
    }

    private void toggleVideoRecording() {
        SoloCameraApi.getApi(drone).toggleVideoRecording(new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                alertUser("Grabando video.");
            }

            @Override
            public void onError(int executionError) {
                alertUser("Error mientras se graba video: " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Timeout mientras se graba video.");
            }
        });
    }

    private void startVideoStream(Surface videoSurface) {
        SoloCameraApi.getApi(drone).startVideoStream(videoSurface, "", true, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                if (stopVideoStream != null)
                    stopVideoStream.setEnabled(true);

                if (startVideoStream != null)
                    startVideoStream.setEnabled(false);
            }

            @Override
            public void onError(int executionError) {
                alertUser("Error mientras comienza el streaming: " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Timed out mientras esperamos al comienzo del streaming.");
            }
        });
    }

    private void stopVideoStream() {
        SoloCameraApi.getApi(drone).stopVideoStream(new SimpleCommandListener() {
            @Override
            public void onSuccess() {
                if (stopVideoStream != null)
                    stopVideoStream.setEnabled(false);

                if (startVideoStream != null)
                    startVideoStream.setEnabled(true);
            }
        });
    }

    //MÉTODOS SET Y GET DE LOS DATOS A OBTENER DEL DRONE

    public String getLatitud( ){
        return this.latitud;
    }

    public void setLatitud(String latitud){
        this.latitud = latitud;
    }

    public String getLongitud( ){
        return this.longitud;
    }

    public void setLongitud(String longitud){
        this.longitud = longitud;
    }

    public String getAltura( ){
        return this.altura;
    }

    public void setAltura(String altura){
        this.altura = altura;
    }

    public String getVelocidad( ){
        return this.velocidad;
    }

    public void setVelocidad(String velocidad){
        this.velocidad = velocidad;
    }
}
