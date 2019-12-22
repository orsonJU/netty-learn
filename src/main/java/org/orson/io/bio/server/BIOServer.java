package org.orson.io.bio.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class BIOServer {
    
    public static final int DEFAULT_PORT = 9090;

    public static void main(String[] args) throws Exception {
        // 绑定本地的localhost:9090
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(DEFAULT_PORT));


        for(;;) {
            // 接收新的客户端连接请求， idea 阻塞
            Socket client = serverSocket.accept();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // try resource
                    try(InputStream is = client.getInputStream()) {
                        byte[] buff = new byte[1024];
                        int len = -1;
                        while((len = is.read(buff)) != -1) {
                            System.out.println(new String(buff, 0, len, Charset.forName("utf8")));
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        System.out.println("client connection closed.");
                    }
                }
            }).start();
        }


    }
}
