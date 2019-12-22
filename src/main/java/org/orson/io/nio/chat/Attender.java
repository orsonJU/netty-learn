package org.orson.io.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Attender {

    private String name;

    public Attender(String name) {
        this.name = name;
    }

    public void connect() {

//        Scanner scanner = new Scanner(System.in);

        Selector selector = null;
        try {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress("localhost", 9999));

            // listen
            selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE|SelectionKey.OP_CONNECT, name);

            //new thread
            /*
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            sc.write(ByteBuffer.wrap((String.format("[%s]", Thread.currentThread().getName()) + " Hello, my name is " + name).getBytes(Charset.forName("UTF-8"))));
                            Thread.sleep(5000);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            */
        }catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }


        for(;;) {

            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();

            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                // if channel is writable
                if(key.isWritable()) {

                    StringBuilder builder = new StringBuilder("Hi my name is " + name + "\r\n");
//                    ByteBuffer byteBuffer = ByteBuffer.allocate(2014);
                    // read from console
                    /*
                    while(scanner.hasNext()) {
                        String message = scanner.next();
                        builder.append(message);
                    }
                    */

                    if(builder.length() > 0) {

                        SocketChannel client = (SocketChannel) key.channel();
                        try {
                            client.write(ByteBuffer.wrap(builder.toString().getBytes(Charset.forName("UTF-8"))));
                            Thread.sleep(3000);
                        }catch(Exception e) {
                            // write failed
                            System.out.println("Sorry, your message sent failed.");
                        }
                    }
                }
                // if is readable, receive message from other attenders
                if(key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                    StringBuilder builder = new StringBuilder();
                    int len = -1;
                    try {
                        while((len = client.read(byteBuffer)) != 0) {
                            builder.append(new String(byteBuffer.array(), 0, len, Charset.forName("UTF-8")));
                        }
                        System.out.println(builder.toString());
                    }catch(IOException e ) {
                        // read failed
                        System.out.println("Sorry, there is a message failed to received.");
                    }
                }
                // only after called finishConnect then the channel is writable
                if(key.isConnectable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    try {
                        client.finishConnect();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        }
    }
}
