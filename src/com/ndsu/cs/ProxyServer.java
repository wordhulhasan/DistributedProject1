package com.ndsu.cs;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ProxyServer implements Runnable{

    String logFileName = "log.txt";

    public static void main(String[] args){
        ProxyServer proxy = new ProxyServer(Integer.parseInt(args[0]));
        proxy.listen();
    }

    private ServerSocket serverSocket;

    static HashMap<String, File> cache;

    static ArrayList<Thread> servicingThreads;

    FileWriter writer;

    BufferedWriter bufferedWriter;


    public ProxyServer(int port){
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

                Thread thread = new Thread(new RequestHandler(socket, this));

                servicingThreads.add(thread);
                thread.start();
            }
        }catch (SocketException e){
            System.out.println("Server is down");
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

    //This is a function which looks redundant but is not. This class implements Runnable and so overrides the run()
    // method of the class Runnable. If our project had requirements such that we had to use this function, we would
    //have used it. For example, this method could have been used for reading input from console continuously and provide
    //input to the program to execute tasks like closeServer(), etc. We could have written some helper class where
    //we would have written some functions like closeServer() which is very specific to the console management or program
    //management of the code.
    @Override
    public void run() {

    }

    public synchronized void writeLog(String info) throws IOException {

        /**
         * To do
         * write string (info) to the log file, and add the current time stamp
         * e.g. String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
         *
         */
        String timeStamp = new SimpleDateFormat("MMM dd yyyy HH:mm:ss z").format(new Date());
        System.out.println(timeStamp+ " "+info);
        writer = new FileWriter(logFileName, true);
        bufferedWriter = new BufferedWriter(writer);
        bufferedWriter.write(timeStamp+" "+info.substring(0,info.length()-9));
        bufferedWriter.newLine();
        bufferedWriter.close();
    }
}