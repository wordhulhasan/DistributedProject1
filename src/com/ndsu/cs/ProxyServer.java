package com.ndsu.cs;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ProxyServer {

    //cache is a Map: the key is the URL and the value is the file name of the file that stores the cached content
    Map<String, String> cache;

    ServerSocket proxySocket;

    String logFileName = "log.txt";

    public static void main(String[] args){
        new ProxyServer().startServer(8081);
    }

    void startServer(int proxyPort){

        cache = new ConcurrentHashMap<>();

        // create the directory to store cached files.
        File cacheDir = new File("cached");
        if (!cacheDir.exists() || (cacheDir.exists() && !cacheDir.isDirectory())) {
            cacheDir.mkdirs();
        }

        /**
         * To do:
         * create a serverSocket to listen on the port (proxyPort)
         * Create a thread (RequestHandler) for each new client connection
         * remember to catch Exceptions!
         *
         */
        ExecutorService executor = null;
        try {
            executor = Executors.newFixedThreadPool(5);

            System.out.println("Waiting for clients..");
            while(true){
                proxySocket = new ServerSocket(proxyPort);
                Socket proxyClientSocket = proxySocket.accept();
                Runnable worker = new RequestHandler(proxyClientSocket, new ProxyServer());

                executor.execute(worker);
            }

        }catch (IOException e){
            System.out.println("Exception caught when trying" +
                    "to listen o port or listening for a connection!");
        }
    }

    public String getCache(String hashcode) {
        return cache.get(hashcode);
    }

    public void putCache(String hashcode, String fileName) {
        cache.put(hashcode, fileName);
    }

    public synchronized void writeLog(String info) {

        /**
         * To do
         * write string (info) to the log file, and add the current time stamp
         * e.g. String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
         *
         */
    }

}