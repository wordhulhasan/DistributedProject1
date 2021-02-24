package com.ndsu.cs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class RequestHandler implements Runnable{

    Socket clientSocket;

    BufferedReader proxyToClientBr;

    BufferedWriter proxyToClientBw;

    private Thread httpClientToServer;

    public RequestHandler(Socket clientSocket){
        this.clientSocket = clientSocket;

        try{
            this.clientSocket.setSoTimeout(5000);
            proxyToClientBr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            proxyToClientBw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        String requestString;

        try {
            requestString = proxyToClientBr.readLine();
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Error in reading request from the client");
            return;
        }

        //parse URL
        System.out.println("The received request is: "+ requestString);

        //request type
        String request = requestString.substring(0, requestString.indexOf(' '));

        //remove request type and space
        String urlString = requestString.substring(requestString.indexOf(' ')+1);

        urlString = urlString.substring(0, urlString.indexOf(' '));

        // Prepend http:// if necessary to create correct URL
        if(!urlString.substring(0,4).equals("http")){
            String temp = "http://";
            urlString = temp + urlString;
        }
        // Check request type
        if(request.equals("CONNECT")){
            System.out.println("HTTPS Request for : " + urlString + "\n");
//            handleHTTPSRequest(urlString);
        }

        else{
            // Check if we have a cached copy
            File file;
            if((file = MultiThreadServer.getCachedPage(urlString)) != null){
                System.out.println("Cached Copy found for : " + urlString + "\n");
                sendCachedPageToClient(file);
            } else {
                System.out.println("HTTP GET for : " + urlString + "\n");
                sendNonCachedToClient(urlString);
            }
        }
    }

    private void sendCachedPageToClient(File fileCached) {

        try{
            //send image files from cache
            String fileExtension = fileCached.getName().substring(fileCached.getName().lastIndexOf('.'));

            String cahcedResponse;

            if ((fileExtension.contains(".png")) || (fileExtension.contains(".jpg")) ||
                    (fileExtension.contains(".gif")) || (fileExtension.contains(".jpeg")) ||(fileExtension.contains(".ico"))){
                BufferedImage image = ImageIO.read(fileCached);

                if (image != null){
                    ImageIO.write(image, fileExtension.substring(1), clientSocket.getOutputStream());
                }
            }
            else {
                BufferedReader cachedFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileCached)));
                String response;

                while((response = cachedFileReader.readLine())!=null){
                    proxyToClientBw.write(response);
                }

                proxyToClientBw.flush();

                if(cachedFileReader != null){
                    cachedFileReader.close();
                }
            }

            if(proxyToClientBw !=null){
                proxyToClientBw.close();
            }


            //send text files from cache


        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Error while sending cached image to client");
        }
    }

    private void sendNonCachedToClient(String urlString) {
        try{

            //This allows the files on stored on disk to resemble that of the URL it was taken from
            int fileExtensionIndex = urlString.lastIndexOf(".");
            String fileExtension;

            // Get the type of file
            fileExtension = urlString.substring(fileExtensionIndex, urlString.length());

            // Get the initial file name
            String fileName = urlString.substring(0,fileExtensionIndex);


            // Trim http://www.
            fileName = fileName.substring(fileName.indexOf('.')+1);

            // Remove any illegal characters
            fileName = fileName.replace("/", "__");
            fileName = fileName.replace('.','_');

            // Trailing / result in index.html of that directory being fetched
            if(fileExtension.contains("/")){
                fileExtension = fileExtension.replace("/", "__");
                fileExtension = fileExtension.replace('.','_');
                fileExtension += ".html";
            }

            fileName = fileName + fileExtension;

            boolean caching = true;
            File fileToCache = null;

            BufferedWriter fileToCacheBw = null;

            try{
                fileToCache = new File("cached/" + fileName);

                if(!fileToCache.exists()){
                    fileToCache.createNewFile();
                }

                fileToCacheBw = new BufferedWriter(new FileWriter(fileToCache));
            }catch (IOException e){
                System.out.println("Couldn't cache: " + fileName);
                caching =false;
                e.printStackTrace();
            }catch (NullPointerException e) {
                System.out.println("Null Pointer Exception while opening the file");
            }

            //process if file is of image type
            if ((fileExtension.contains(".png")) || (fileExtension.contains(".jpg")) ||
                    (fileExtension.contains(".gif")) || (fileExtension.contains(".jpeg")) ||(fileExtension.contains(".ico"))){
                try{
                    URL remoteURL = new URL(urlString);

                    BufferedImage image = ImageIO.read(remoteURL);

                    if(image != null){
                        ImageIO.write(image, fileExtension.substring(1), fileToCache);

                        ImageIO.write(image, fileExtension.substring(1), fileToCache);
                    }

                }catch (Exception e){
                    System.out.println("Couldn't read the image file");;
                }
            }


            else {
                URL remoteURL = new URL(urlString);
                HttpURLConnection proxyToRemoteCon = (HttpURLConnection) remoteURL.openConnection();
                proxyToRemoteCon.setUseCaches(false);
                proxyToRemoteCon.setDoOutput(true);

                BufferedReader proxyToRemoteBR = new BufferedReader(new InputStreamReader(proxyToRemoteCon.getInputStream()));
                String line;
                while((line = proxyToRemoteBR.readLine()) != null){
                    proxyToClientBw.write(line);

                    if (caching){
                        fileToCacheBw.write(line);
                    }
                }


                proxyToClientBw.flush();

                if(proxyToRemoteBR != null){
                    proxyToRemoteBR.close();
                }
            }

            if (caching){
                fileToCacheBw.flush();
                MultiThreadServer.addCachedPage(urlString, fileToCache);
            }

            if(fileToCacheBw != null){
                fileToCacheBw.close();
            }

            if(proxyToClientBw != null){
                proxyToClientBw.close();
            }



        }catch (Exception e){
            e.printStackTrace();
        }

    }
}