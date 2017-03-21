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
        ChatOperator chatOperator = new ChatOperator();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            chatOperator.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
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

                        text = "32"; //항상 랜딩시키고 연결 종료 메세지 전송
                        chatOperator.MessageSend(text);

                        text = "113";
                        chatOperator.MessageSend(text);

                        text = "27";
                        chatOperator.MessageSend(text);
                    }
                    try {
                        if(!client.isClosed()){
                            Thread.sleep(100);
                            client.close();
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
                        String text = "101";
                        MessageSend(text);
                    }
                }
            });

            buttonLandTakeOff.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    String text = "";

                    if (buttonLandTakeOff.getText() == "Take Off") {
                        ConnectionTrue = true;
                        text = "116";
                        buttonLandTakeOff.setText("Landing");

                    } else {
                        ConnectionTrue = false;
                        text = "32";
                        buttonLandTakeOff.setText("Take Off");
                    }
                    MessageSend(text);
                }
            });

            buttonForward.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String text = "114";
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
                    String text = "102";
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
                    String text = "100";
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
                    String text = "103";
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
                    String text = "65";
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
                    String text = "66";
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
                    String text = "67";
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
                    String text = "68";
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
            String text = "-1";
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
}
