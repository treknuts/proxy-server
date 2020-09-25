package Proxy;

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



public class ProxyServer {

    //cache is a Map: the key is the URL and the value is the file name of the file that stores the cached content
    Map<String, String> cache;

    ServerSocket proxySocket;

    String logFileName = "log.txt";

    boolean listening = true;

    public static void main(String[] args) throws IOException {
        new ProxyServer().startServer(5678);
    }

    void startServer(int proxyPort) throws IOException {

        cache = new ConcurrentHashMap<>();

        // create the directory to store cached files.
        File cacheDir = new File("cached");
        if (!cacheDir.exists() || (cacheDir.exists() && !cacheDir.isDirectory())) {
            cacheDir.mkdirs();
        }

        /*
         * TODO: Create a server socket to listen on specified port
         * create a serverSocket to listen on the port (proxyPort)
         * Create a thread (RequestHandler) for each new client connection
         * remember to catch Exceptions!
         */
        try {
            proxySocket = new ServerSocket(proxyPort);
            proxySocket.getInetAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (listening) {
            new RequestHandler(proxySocket.accept(), this).start();
        }
        proxySocket.close();
    }



    public String getCache(String hashcode) {
        return cache.get(hashcode);
    }

    public void putCache(String hashcode, String fileName) {
        cache.put(hashcode, fileName);
    }

    public synchronized void writeLog(String info) {

        /*
         * TODO: write info to log file with timestamp
         * write string (info) to the log file, and add the current time stamp
         * e.g. String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
         */



    }

}
