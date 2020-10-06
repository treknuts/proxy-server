package Proxy;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
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
            clientSocket.setSoTimeout(5000);
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

             this.server.writeLog(urlToCall);

             if (this.server.cache.containsKey(urlToCall)) {
                 sendCachedInfoToClient(this.server.getCache(urlToCall));
             } else {
                 if (tokens[0].equals("GET")) {
                     proxyServertoClient(request, urlToCall);
                 }
             }

         } catch (Exception e) {
             e.printStackTrace();
         }

    }


    private boolean proxyServertoClient(byte[] clientRequest, String url) throws IOException {

        Socket serverSocket;
        URL urlObj = new URL(url);
        String host = urlObj.getHost();
        String urlFile = urlObj.getFile();
        PrintWriter outToServer = null;
        PrintWriter clientOut = null;
        BufferedReader serverIn = null;
        System.out.println(url);
        System.out.println(host);

        // Create Buffered output stream to write to cached copy of file
        String fileName = "cached/" + generateRandomFileName() + ".dat";

        // to handle binary content, byte is used
        byte[] serverReply = new byte[4096];

        serverSocket = new Socket();
        try {
            serverSocket.connect(new InetSocketAddress(host, 80));
            outToServer = new PrintWriter(serverSocket.getOutputStream(), true);
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

            serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }

        String request = "GET " + urlFile + " HTTP/1.1";
        System.out.println(request);
        outToServer.println(request);
        outToServer.println("Host: " + host);
        outToServer.println("");

        StringBuffer response = new StringBuffer();
        String readLine;

        if(urlFile.contains(".jpg") || urlFile.contains(".png") || urlFile.contains(".gif") || urlFile.contains(".ttf") || urlFile.contains(".ico")
                || urlFile.contains(".GIF") || urlFile.contains(".swf") || urlFile.contains(".JPG") || urlFile.contains(".PNG")
                || urlFile.contains(".jpeg") || urlFile.contains(".JPEG")) {
            InputStream inFromServer = serverSocket.getInputStream();
            OutputStream outToClient = clientSocket.getOutputStream();

            int read;

            while ((read = inFromServer.read(serverReply)) != -1) {
                outToClient.write(serverReply, 0, read);
                outToClient.flush();
            }
            inFromServer.close();

        } else {
            if (fileName.length() < 220) {
            File file = new File(fileName);

            if (!file.exists()) {
                file.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true));

                    while ((readLine = serverIn.readLine()) != null) {
                        response.append(readLine + "\n");
                        clientOut.println(readLine);
                        bw.write(readLine);
                        bw.newLine();
                    }
                    bw.close();
                    System.out.println(response.toString());
                    this.server.putCache(url, fileName);
                }
            }
        }

        outToServer.flush();
        clientOut.flush();
        serverIn.close();

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
