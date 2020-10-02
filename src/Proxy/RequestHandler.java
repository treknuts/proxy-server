package Proxy;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.StringTokenizer;


// RequestHandler is thread that process requests of one client connection
public class RequestHandler extends Thread {


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
         * TODO: Process client requests
         * Process the requests from a client. In particular,
         * (1) Check the request type, only process GET request and ignore others
         * (2) If the url of GET request has been cached, respond with cached content
         * (3) Otherwise, call method proxyServertoClient to process the GET request
         */
         try {
             proxyToClientBufferedReader = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream())
             );

             String inputLine;
             int count = 0;
             String urlToCall = "";
             String[] tokens = new String[100];
             while((inputLine = proxyToClientBufferedReader.readLine()) != null) {
                 try {
                     StringTokenizer tokenizer = new StringTokenizer(inputLine);
                     tokenizer.nextToken();
                 } catch (Exception e) {
                     break;
                 }

                 if (count == 0) {
                     tokens = inputLine.split(" ");
                     urlToCall = tokens[1];
                     System.out.println("Request for : " + urlToCall);
                     System.out.println(tokens[count]);
                 }
                 count++;
             }

             if(!urlToCall.substring(0,4).equals("http")){
                 String temp = "http://";
                 urlToCall = temp + urlToCall;
             }

             if (this.server.cache.containsKey(urlToCall)) {
                 sendCachedInfoToClient(urlToCall);
             } else {
                 if (tokens[0].equals("GET")) {
                     proxyServertoClient(request, urlToCall);
                 }
             }

             for (String str : tokens) {
                 System.out.println(str);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }

    }


    private boolean proxyServertoClient(byte[] clientRequest, String url) throws IOException {

        FileOutputStream fileWriter;
        Socket serverSocket;
        InputStream inFromServer;
        OutputStream outToServer;

        // Create Buffered output stream to write to cached copy of file
        String fileName = "cached/" + generateRandomFileName() + ".dat";

        // to handle binary content, byte is used
        byte[] serverReply = new byte[4096];

        /*
         * TODO: At this point I would have needed to parse client request
         * (1) Create a socket to connect to the web server (default port 80)
         * (2) Send client's request (clientRequest) to the web server, you may want to use flush() after writing.
         * (3) Use a while loop to read all responses from web server and send back to client
         * (4) Write the web server's response to a cache file, put the request URL and cache file name to the cache Map
         * (5) close file, and sockets.
         */


        try {
            outToServer = new BufferedOutputStream(clientSocket.getOutputStream());
            serverSocket = new Socket(url, 80);

            outToServer.write(clientRequest);
            outToServer.flush();

            // TODO: get the motherfucking server response
            inFromServer = serverSocket.getInputStream();
            while (inFromServer.read() != -1) {

            }
            outToClient.write(serverReply);
            outToClient.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
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

}
