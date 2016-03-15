package com.appspot.idrondataanalyzer.droneapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.interfaces.TowerListener;

public class MainActivity extends ActionBarActivity implements TowerListener {

    private ControlTower controlTower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the service manager
        this.controlTower = new ControlTower(getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        this.controlTower.connect(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        this.controlTower.disconnect();
    }

    // 3DR Services Listener
    @Override
    public void onTowerConnected() {

    }

    @Override
    public void onTowerDisconnected() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
