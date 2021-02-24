package com.ndsu.cs;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MultiThreadServer implements Runnable{

    public static void main(String[] args){
        MultiThreadServer proxy = new MultiThreadServer(8081);
        proxy.listen();
    }

    private ServerSocket serverSocket;

    static HashMap<String, File> cache;

    static ArrayList<Thread> servicingThreads;


    public MultiThreadServer(int port){
        cache = new HashMap<>();
        servicingThreads = new ArrayList<>();

        new Thread(this).start();

        try{
            File cachedSites = new File("cachedSites.txt");
            if(!cachedSites.exists()){
                System.out.println("No cached site found, creating new file..");
            }else{
                FileInputStream fileInputStream = new FileInputStream(cachedSites);
                ObjectInputStream  objectInputStream = new ObjectInputStream(fileInputStream);
                cache = (HashMap<String, File>) objectInputStream.readObject();
                fileInputStream.close();
                objectInputStream.close();
            }
        }catch (IOException e){
            System.out.println("Error Loading Previously cached sites file");
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            System.out.println("Class not found for previously loading cached sites file");
            e.printStackTrace();
        }

        try{
            serverSocket = new ServerSocket(8081);

            System.out.println("Waiting for client on port: "+serverSocket.getLocalPort()+"...");


        }catch (IOException e){
            System.out.println("Exception when connecting to the client");
        }
    }
    private volatile boolean running = true;

    private void listen() {
        try{
            while(running){
                Socket socket = serverSocket.accept();

                Thread thread = new Thread(new RequestHandler(socket));

                servicingThreads.add(thread);
                thread.start();
            }
        }catch (SocketException e){
            System.out.println("Server is down");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void closeServer() {
        System.out.println("Closing the proxy server..");

        running = false;

        try{
            FileOutputStream fileOut = new FileOutputStream("cachedSites.txt");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOut);

            objectOutputStream.writeObject(cache);
            objectOutputStream.close();
            fileOut.close();
            System.out.println("Cached Sites written");

            try{
                System.out.println("Closing connection..");
                serverSocket.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void addCachedPage(String urlString, File fileToCache){
        cache.put(urlString, fileToCache);
    }

    public static File getCachedPage(String url){
        File file = null;
        try{
            file =  cache.get(url);
        }catch (Exception e){
            System.out.println("Hashmap returned null");
        }
        return file;
    }

    @Override
    public void run() {
//        Scanner scanner = new Scanner(System.in);
//
//        String command;
//        while(running){
//            System.out.println("Enter new site to block, or type \"blocked\" to see blocked sites, \"cached\" to see cached sites, or \"close\" to close server.");
//            command = scanner.nextLine();
//
//
//            if(command.toLowerCase().equals("cached")){
//                System.out.println("\nCurrently Cached Sites");
//                for(String key : cache.keySet()){
//                    System.out.println(key);
//                }
//                System.out.println();
//            }
//
//
//            else if(command.equals("close")){
//                running = false;
//                closeServer();
//            }
//
//        }
//        scanner.close();
    }
}