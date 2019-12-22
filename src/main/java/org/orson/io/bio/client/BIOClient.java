package org.orson.io.bio.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class BIOClient {

    public static void main(String[] args)  {

        BIOClient bioClient = new BIOClient();

        for(int i = 0; i < 100; i++ ) {
            RandomClient randomClient = bioClient.new RandomClient("Client-" + i);
            // start a thread
            new Thread(randomClient).start();

        }

    }


    /**
     * A fake client
     */
    class RandomClient implements Runnable {

        private String message;

        public RandomClient(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            Socket socket = new Socket();
            try {
                // connect to server
                socket.connect(new InetSocketAddress("localhost", 9090));
                // print the message to server
                OutputStream os = socket.getOutputStream();
                os.write(String.format("Hello, this is client %s", message).getBytes(Charset.forName("UTF-8")));
                os.flush();
            }catch(IOException e ) {
                e.printStackTrace();
            }finally {
                try{
                    socket.close();
                }catch (Exception e) {
                    // skip
                }
            }

        }
    }

}
