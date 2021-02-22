package com.ndsu.cs;

import com.oracle.tools.packager.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// RequestHandler is thread that process requests of one client connection
public class RequestHandler implements Runnable {


    Socket clientSocket;

    InputStream inFromClient;

    OutputStream outToClient;

    byte[] request = new byte[1024];

    BufferedReader proxyToClientBufferedReader;

    BufferedWriter proxyToClientBufferedWriter;

    BufferedReader bufReadClientRequest;

    String requestString;

    private ProxyServer server;

    StringBuffer serverResponse = new StringBuffer();


    public RequestHandler(Socket clientSocket, ProxyServer proxyServer) {


        this.clientSocket = clientSocket;


        this.server = proxyServer;


        try {
            clientSocket.setSoTimeout(15000);
            inFromClient = clientSocket.getInputStream();
            outToClient = clientSocket.getOutputStream();
            bufReadClientRequest = new BufferedReader(new InputStreamReader(inFromClient));


        } catch (IOException e) {
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
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outToClient));
            while (true) {
                requestString = bufReadClientRequest.readLine();
                if (requestString != null && requestString.contains("GET")) {
                    proxyServertoClient(requestString);

                } else {
                }
            }
        } catch (IOException e) {
            System.out.println("Error");
        }
    }


    private void proxyServertoClient(String requestString) throws IOException {

        FileOutputStream fileWriter = null;
        Socket serverSocket = null;
        InputStream inFromServer;
        OutputStream outToServer;

        // Create Buffered output stream to write to cached copy of file
        String fileName = "cached/" + generateRandomFileName() + ".dat";

        // to handle binary content, byte is used
        byte[] serverReply = new byte[4096];

        URL treatURL;


        /**
         * To do
         * (1) Create a socket to connect to the web server (default port 80)
         * (2) Send client's request (clientRequest) to the web server, you may want to use flush() after writing.
         * (3) Use a while loop to read all responses from web server and send back to client
         * (4) Write the web server's response to a cache file, put the request URL and cache file name to the cache Map
         * (5) close file, and sockets.
         */
        String urlString = parseUrl(requestString);
        System.out.println(urlString);
        int remotePort = 80;
        InetAddress address = InetAddress.getByName(new URL(urlString).getHost());
        try {
            serverSocket = new Socket(address, remotePort);
            serverSocket.setSoTimeout(30000);

            URL remoteURL = new URL(urlString);
            HttpURLConnection proxyToServerConnection = (HttpURLConnection)remoteURL.openConnection();

            int responseCode = proxyToServerConnection.getResponseCode();

            if (responseCode == 200){
                BufferedReader fromServer = new BufferedReader(new InputStreamReader(proxyToServerConnection.getInputStream()));

                String input;
                while ((input = fromServer.readLine())!=null){
                    serverResponse.append(input);
                }
                fromServer.close();

            }
            System.out.println(serverResponse);

            fileWriter = new FileOutputStream(fileName);

            inFromServer = serverSocket.getInputStream();
            BufferedReader bufferReaderFromServerToProxy = new BufferedReader(new InputStreamReader(inFromServer));

            outToServer = serverSocket.getOutputStream();
            BufferedWriter bufferedWriterFromProxyToServer = new BufferedWriter(new OutputStreamWriter(outToServer));


            fileWriter.write(serverReply);

            try{
                int byteRead;

                while((byteRead = inFromServer.read(serverReply)) != -1){
                    outToClient.write(serverReply, 0, byteRead);
                    outToClient.flush();
                }


            }catch (Exception e){
                e.printStackTrace();
            }

            if (serverSocket != null){
                serverSocket.close();
            }
            if(proxyToClientBufferedWriter != null){
                proxyToClientBufferedWriter.close();
            }
            if(bufferedWriterFromProxyToServer != null){
                bufferedWriterFromProxyToServer.close();
            }
            if(bufferReaderFromServerToProxy != null){
                bufferReaderFromServerToProxy.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }



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
        System.out.println("Line: " + line);
        Pattern p = Pattern.compile(Pattern.quote("GET ") + "(.*?)" + Pattern.quote(" HTTP/"));
        Matcher m = p.matcher(line);

        String url = "";

        while (m.find()) {
            url = m.group(1);
        }

        return url;
    }

}