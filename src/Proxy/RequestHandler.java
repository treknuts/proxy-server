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
        // Get host name from request

        try {
            DataOutputStream out =
                    new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            String inputLine, outputLine;
            int i = 0;
            String urlToCall = "";

            //begin get request from client
            while ((inputLine = in.readLine()) != null) {
                try {
                    StringTokenizer tok = new StringTokenizer(inputLine);
                    tok.nextToken();
                } catch (Exception e) {
                    break;
                }
                if (i == 0) {
                    String[] tokens = inputLine.split(" ");

                    urlToCall = tokens[1];
                    System.out.println(tokens[0]);
                    System.out.println("Requested URL: " + urlToCall);
                }
                i++;
            }

            BufferedReader rd = null;
            try {
                URL hostname = new URL(urlToCall);
                URLConnection connection = hostname.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(false);
                InputStream is = null;
                HttpURLConnection httpConn = (HttpURLConnection) connection;
                if (connection.getContentLength() > 0) {
                    try {
                        is = connection.getInputStream();
                        rd = new BufferedReader(new InputStreamReader(is));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                int index = is.read(request, 0, 1024);
                while (index != -1) {
                    out.write(request, 0, index);
                    index = is.read(request, 0, 1024);
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //close out all resources
            if (rd != null) {
                rd.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private boolean proxyServertoClient(byte[] clientRequest) throws IOException {

        FileOutputStream fileWriter = null;
        Socket serverSocket = null;
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
        outToServer = this.clientSocket.getOutputStream();
        inFromServer = this.clientSocket.getInputStream();

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
