package com.example.pyojihye.smart2lte;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static com.example.pyojihye.smart2lte.Const.IP;
import static com.example.pyojihye.smart2lte.Const.PORT;

/**
 * Created by nsc1303-PJH on 2016-11-13.
 */

public class FlightActivity extends AppCompatActivity {
    private Button buttonEmergency;

    private Button buttonLandTakeOff;

    //roll
    private Button buttonForward;
    private Button buttonBack;
    private Button buttonRollLeft;
    private Button buttonRollRight;

    //yaw
    private Button buttonUp;
    private Button buttonDown;
    private Button buttonRight;
    private Button buttonLeft;

    private Socket client;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    private boolean ConnectionTrue;
    private boolean first;

    static public final String PROTO_DVTYPE_KEY = "DVTYPE";
    static public final String PROTO_MSG_TYPE_KEY = "MSGTYPE";

    public enum PROTO_DVTYPE {
        PHONE, DRONE
    };

    public enum PROTO_MSGTYPE {
        CMD, GPS, PICTURE, HELLO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);
        buttonEmergency = (Button) findViewById(R.id.buttonEmergency);

        buttonLandTakeOff = (Button) findViewById(R.id.buttonLandTakeOff);

        buttonForward = (Button) findViewById(R.id.buttonForward);
        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonRollLeft = (Button) findViewById(R.id.buttonRollLeft);
        buttonRollRight = (Button) findViewById(R.id.buttonRollRight);

        buttonUp = (Button) findViewById(R.id.buttonUp);
        buttonDown = (Button) findViewById(R.id.buttonDown);
        buttonRight = (Button) findViewById(R.id.buttonRight);
        buttonLeft = (Button) findViewById(R.id.buttonLeft);
        buttonLandTakeOff.setText("Take Off");

        ConnectionTrue = false;
        first=false;
        ChatOperator chatOperator = new ChatOperator();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            chatOperator.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        else
            chatOperator.execute();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setTitle(getString(R.string.dialog_title));
            d.setMessage(getString(R.string.dialog_contents));
            d.setIcon(R.mipmap.ic_launcher);

            d.setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String text;

                    if (ConnectionTrue) {

                        ChatOperator chatOperator = new ChatOperator();

                        text = protocolSet("32", first);
                        chatOperator.MessageSend(text);

                        text = protocolSet("113", first);
                        chatOperator.MessageSend(text);

                        text = protocolSet("27", first);
                        chatOperator.MessageSend(text);
                    }
                    try {
                        if (!client.isClosed()) {
                            Thread.sleep(100);
                            client.close();
                            first=false;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            });

            d.setNegativeButton(getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            d.show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class ChatOperator extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                client = new Socket(IP, PORT);
//                Log.d("Log","IP : "+IP+"\nPORT:"+PORT);

                if (client != null) {
                    ConnectionTrue = true;
                    printWriter = new PrintWriter(client.getOutputStream(), true);
                    InputStreamReader inputStreamReader = new InputStreamReader((client.getInputStream()));
                    bufferedReader = new BufferedReader(inputStreamReader);
                    String text = protocolSet("", first);
                    MessageSend(text);
                    first=true;
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.snack_bar_server_port), Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.snack_bar_server_connect), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return null;
        }

        private void MessageSend(final String text) {
            final Sender messageSender = new Sender();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                messageSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, text);
            } else {
                messageSender.execute(text);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            buttonEmergency.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (ConnectionTrue) {
                        String text = protocolSet("101", first);
                        MessageSend(text);
                    }
                }
            });

            buttonLandTakeOff.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    String text = "";

                    if (buttonLandTakeOff.getText() == "Take Off") {
                        ConnectionTrue = true;
                        text = protocolSet("116", first);
                        buttonLandTakeOff.setText("Landing");

                    } else {
                        ConnectionTrue = false;
                        text = protocolSet("32", first);
                        buttonLandTakeOff.setText("Take Off");
                    }
                    MessageSend(text);
                }
            });

            buttonForward.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String text = protocolSet("114", first);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (ConnectionTrue) {
                                MessageSend(text);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            DroneDefault();
                            break;
                    }
                    return true;
                }
            });

            buttonBack.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String text = protocolSet("102", first);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (ConnectionTrue) {
                                MessageSend(text);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            DroneDefault();
                            break;
                    }
                    return true;
                }
            });

            buttonRollLeft.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String text = protocolSet("100", first);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (ConnectionTrue) {
                                MessageSend(text);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            DroneDefault();
                            break;
                    }
                    return true;
                }
            });

            buttonRollRight.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String text = protocolSet("103", first);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (ConnectionTrue) {
                                MessageSend(text);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            DroneDefault();
                            break;
                    }
                    return true;
                }
            });

            buttonUp.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String text = protocolSet("65", first);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (ConnectionTrue) {
                                MessageSend(text);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            DroneDefault();
                            break;
                    }
                    return true;
                }
            });

            buttonDown.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String text = protocolSet("66", first);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (ConnectionTrue) {
                                MessageSend(text);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            DroneDefault();
                            break;
                    }
                    return true;
                }
            });

            buttonRight.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String text = protocolSet("67", first);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (ConnectionTrue) {
                                MessageSend(text);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            DroneDefault();
                            break;
                    }
                    return true;
                }
            });

            buttonLeft.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String text = protocolSet("68", first);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (ConnectionTrue) {
                                MessageSend(text);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            DroneDefault();
                            break;
                    }
                    return true;
                }
            });

            if (client != null) {
                Receiver receiver = new Receiver();
                receiver.execute();
            }
        }

        private void DroneDefault() {
            String text = protocolSet("-1", first);
            MessageSend(text);
        }
    }

    private class Sender extends AsyncTask<String, String, Void> {
        private String message;

        @Override
        protected Void doInBackground(String... params) {
            message = params[0];
            printWriter.write(message + "\n");
            printWriter.flush();
            return null;
        }
    }

    private class Receiver extends AsyncTask<Void, Void, Void> {
        private String message;

        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                try {
                    if (bufferedReader.ready()) {
                        message = bufferedReader.readLine();
                        publishProgress(null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
//            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
//            View v = snackbar.getView();
//            TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
//            textView.setTextColor(Color.WHITE);
//            snackbar.show();
        }
    }

    private String protocolSet(String str, boolean first) {
        String msg = "";
        if (!first) { //첫 연결
            msg = PROTO_DVTYPE_KEY + "=" + PROTO_DVTYPE.PHONE.ordinal() + "%%" + PROTO_MSG_TYPE_KEY + "=" + PROTO_MSGTYPE.HELLO.ordinal();
        } else {
            msg = PROTO_DVTYPE_KEY + "=" + PROTO_DVTYPE.PHONE.ordinal() + "%%" + PROTO_MSG_TYPE_KEY + "=" + PROTO_MSGTYPE.CMD.ordinal() + "%%DATA=" + str;
        }
        return msg;
    }
}
