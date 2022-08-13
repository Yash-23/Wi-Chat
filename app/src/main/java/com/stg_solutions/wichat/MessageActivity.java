package com.stg_solutions.wichat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MessageActivity extends AppCompatActivity {
    static final int READ_MESSAGE = 1;
    TextView readMsg;
    EditText typeMessage;
    ImageButton sendButton;
    boolean owner;


    private Socket socket;
    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    public static final int SERVER_PORT = 6000;
    private static String SERVER_IP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Intent intent = getIntent();

        owner = intent.getBooleanExtra("Owner?",false);
        SERVER_IP = intent.getStringExtra("Owner Address");



        sendButton = (ImageButton) findViewById(R.id.sendButton);
        typeMessage = (EditText) findViewById(R.id.typeMessage);
        readMsg = (TextView) findViewById(R.id.message_output);

        updateConversationHandler = new Handler();
        if(owner){
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
        }else{
            new Thread(new ClientThread()).start();
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String str = typeMessage.getText().toString();
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                    out.println(str);
                }catch(UnknownHostException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                } catch(NullPointerException e){
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }



    // Opens a socket on the owner device to get messages
    class ServerThread implements Runnable {
        @Override
        public void run() {
            Socket socket = null;
            try {
                // Create a socket on port 6000
                serverSocket = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Start listening for messages
                    socket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Handles received messages from clients
    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                // read received data
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    updateConversationHandler.post(new UpdateUIThread(read));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Handles showing received messages on screen
    class UpdateUIThread implements Runnable {
        private String msg;

        public UpdateUIThread(String str) {
            this.msg = str;
        }

        // Print message on screen
        @Override
        public void run() {
            readMsg.setText(readMsg.getText().toString() + "incoming message: " + msg + "\n");
        }
    }

    // Handles connection to server
    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddress, SERVER_PORT);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


