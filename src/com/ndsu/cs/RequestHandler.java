package com.ndsu.cs;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// RequestHandler is thread that process requests of one client connection
public class RequestHandler extends Thread implements Runnable {


    Socket clientSocket;

    InputStream inFromClient;

    OutputStream outToClient;

    byte[] request = new byte[1024];

    BufferedReader proxyToClientBufferedReader;

    BufferedWriter proxyToClientBufferedWriter;


    private ProxyServer server;


    public RequestHandler(Socket clientSocket, ProxyServer proxyServer) {


        this.clientSocket = clientSocket;


        this.server = proxyServer;

        try {
            clientSocket.setSoTimeout(2000);
            inFromClient = clientSocket.getInputStream();
            outToClient = clientSocket.getOutputStream();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override

    public void run() {

        /**
         * To do
         * Process the requests from a client. In particular,
         * (1) Check the request type, only process GET request and ignore others
         * (2) If the url of GET request has been cached, respond with cached content
         * (3) Otherwise, call method proxyServertoClient to process the GET request
         *
         */

        System.out.println("Thread started with name: " + Thread.currentThread().getName());
        String userInput;

        proxyToClientBufferedReader = new BufferedReader(new InputStreamReader(inFromClient));
        proxyToClientBufferedWriter = new BufferedWriter(new OutputStreamWriter(outToClient));

        while (true) {
            try {
                userInput = proxyToClientBufferedReader.readLine();
                while (!(userInput.isEmpty())) {
                    System.out.println(userInput);

                    userInput = proxyToClientBufferedReader.readLine();
                    proxyToClientBufferedWriter.write("You entered : " + userInput);
                    proxyToClientBufferedWriter.newLine();
                    proxyToClientBufferedWriter.flush();
                }
                ;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                System.out.println("Exception in Thread Run. Exception: " + ex);
            }

        }

    }


    private boolean proxyServertoClient(byte[] clientRequest) {

        FileOutputStream fileWriter = null;
        Socket serverSocket = null;
        InputStream inFromServer;
        OutputStream outToServer;

        // Create Buffered output stream to write to cached copy of file
        String fileName = "cached/" + generateRandomFileName() + ".dat";

        // to handle binary content, byte is used
        byte[] serverReply = new byte[4096];


        /**
         * To do
         * (1) Create a socket to connect to the web server (default port 80)
         * (2) Send client's request (clientRequest) to the web server, you may want to use fluch() after writing.
         * (3) Use a while loop to read all responses from web server and send back to client
         * (4) Write the web server's response to a cache file, put the request URL and cache file name to the cache Map
         * (5) close file, and sockets.
         */
        return false;

    }


    // Sends the cached content stored in the cache file to the client
    private void sendCachedInfoToClient(String fileName) {

        try {

            byte[] bytes = Files.readAllBytes(Paths.get(fileName));

            outToClient.write(bytes);
            outToClient.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            if (clientSocket != null) {
                clientSocket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    // Generates a random file name
    public String generateRandomFileName() {

        String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
        SecureRandom RANDOM = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; ++i) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public String parseUrl(String line) {
        Pattern p = Pattern.compile(Pattern.quote("GET ") + "(.*?)" + Pattern.quote(" HTTP/"));
        Matcher m = p.matcher(line);

        String url = "";

        while (m.find()) {
            url =  m.group(1);
        }

        return url;
    }

}