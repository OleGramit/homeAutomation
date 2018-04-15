package com.og.home.homeautomation;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.og.home.homeautomation.helper.MqttHelper;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{

    final String brokerUri = "tcp://192.168.1.102:1883";
    final String turnOnString = "turn_On";
    final String turnOffString = "turn_Off";

    String mClientId;
    MqttAndroidClient mClient;
    String mToastString = null;

    //Buttons
    private ToggleButton BedroomLight;
    private boolean BedroomLightState = false;

    private ToggleButton BedroomPcStecker;
    private boolean BedroomPcSteckerState = false;

    private ToggleButton BedroomLightDoor;
    private boolean BedroomLightDoorState = false;


    //Steckdosen Topics
    String topicSteckdose01 = "home/bedroom/steckdose01";
    String topicSteckdose02 = "home/bedroom/steckdose02";
    String topicSteckdose03 = "home/bedroom/steckdose03";
    String topicSteckdose04 = "home/bedroom/steckdose04";
    String topicSteckdose05 = "home/bedroom/steckdose05";

    //Andere Topics
    String topicRootPi =  "home/root/Pi";
    String topicRootPhone =  "home/root/Phone";


    String[] topicList = new String[7];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create topic List
        topicList[0]=topicSteckdose01;
        topicList[1]=topicSteckdose02;
        topicList[2]=topicSteckdose03;
        topicList[3]=topicSteckdose04;
        topicList[4]=topicSteckdose05;
        topicList[5]=topicRootPhone;
        topicList[6]=topicRootPi;


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Button OnClickListener
        BedroomLight = findViewById(R.id.BedroomLight);
        BedroomLight.setOnClickListener(this);

        BedroomPcStecker = findViewById(R.id.BedroomPcStecker);
        BedroomPcStecker.setOnClickListener(this);

        BedroomLightDoor = findViewById(R.id.BedroomLightDoor);
        BedroomLightDoor.setOnClickListener(this);

        connectToBroker();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Favoriten) {
            // Handle the camera action
        } else if (id == R.id.nav_bedroom) {

        } else if (id == R.id.nav_livingroom) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onClick(View view){
        Log.d("ONCLICK","Any Button Pressed");
        if(mClient.isConnected()==false){
            connectToBroker();
        }
        //MqttHelper mMqttPublishHelper;
        switch (view.getId()) {
            case R.id.BedroomLight:
                if(BedroomLightState == true) {
                    publish(topicSteckdose01, turnOffString);
                    publish(topicSteckdose04, turnOffString);
                    publish(topicSteckdose05, turnOffString);
                    BedroomLightState = false;
                }
                else if (BedroomLightState == false){
                    publish(topicSteckdose01, turnOnString);
                    publish(topicSteckdose04, turnOnString);
                    publish(topicSteckdose05, turnOnString);
                    BedroomLightState = true;
                }
                break;
            case R.id.BedroomPcStecker:
                if(BedroomPcSteckerState == true) {
                    publish(topicSteckdose02, turnOffString);
                    publish(topicSteckdose03, turnOffString);
                    BedroomPcSteckerState = false;
                }
                else if (BedroomPcSteckerState == false){
                    publish(topicSteckdose02, turnOnString);
                    publish(topicSteckdose03, turnOnString);
                    BedroomPcSteckerState = true;
                }
                break;
            case R.id.BedroomLightDoor:
                if(BedroomLightDoorState == true) {
                    publish(topicSteckdose04, turnOffString);
                    BedroomLightDoorState = false;
                }
                else if (BedroomLightDoorState == false){
                    publish(topicSteckdose04, turnOnString);
                    BedroomLightDoorState = true;
                }
                break;

        }
    }


    private void connectToBroker(){
        mClientId = MqttClient.generateClientId();
        mClient = new MqttAndroidClient(this.getApplicationContext(),brokerUri,mClientId);

        try {
            IMqttToken mToken = mClient.connect();

            mToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    mToastString = "Connected";
                    Toast.makeText(MainActivity.this, mToastString, Toast.LENGTH_LONG).show();
                    Log.d("CONNECT", "onSuccess");
                    publish("home/root/phone","connected");


                    //Subscribe to All Topics
                    final String topic = "home/bedroom/test";
                    int qos = 1;
                    try {
                        IMqttToken subToken = mClient.subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Toast.makeText(MainActivity.this,"Subscribed to:" + topic,Toast.LENGTH_LONG);

                                //Subscribe to all topics
                                for (int i =0; i <= topicList.length;i++) {

                                    try {
                                        mClient.subscribe(topicList[i],1);
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                Toast.makeText(MainActivity.this,"Failure while subscribing to:" + topic,Toast.LENGTH_LONG);
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards

                            }
                        });

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    mToastString = "Connection Failure";
                    Toast.makeText(MainActivity.this, mToastString, Toast.LENGTH_LONG).show();
                    Log.d("CONNECT", "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void messageArrived(String topic, MqttMessage message){

    }

    private void publish(String mTopic,String mPayload){
        byte[] encodedPayload = new byte[0];
        try {
            Log.d("Publish","in Publish");
            encodedPayload = mPayload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            mClient.publish(mTopic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

}


