package org.orson.io.nio.client;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NIOClient {

    public static void main(String[] args) throws  Exception {

        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);

        sc.connect(new InetSocketAddress("localhost", 9090));


        Selector selector = Selector.open();
        sc.register(selector, SelectionKey.OP_READ| SelectionKey.OP_WRITE|SelectionKey.OP_CONNECT);


        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//        sc.write(ByteBuffer.wrap("Hello Server, this is orson.".getBytes(Charset.forName("UTF-8"))));

        while (true) {

            try {
                selector.select();

            } catch (Exception e) {
                e.printStackTrace();
                //skip
            }

            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();

            while(iterator.hasNext()) {
                System.out.println("client invoke readable.");

                SelectionKey key = iterator.next();
                iterator.remove();

                if(key.isReadable()) {

                    SocketChannel client = (SocketChannel) key.channel();


                    int read = client.read(byteBuffer);

                    System.out.println(new String(byteBuffer.array(),Charset.forName("UTF-8")));


                }
                // after server accepted, should be writeable
                else if(key.isWritable()) {
                    byte[] bytes = "Hello Server, this is orson.".getBytes(Charset.forName("UTF-8"));
                    int len = bytes.length;
                    byteBuffer.clear();
                    byteBuffer.put(bytes);
                    byteBuffer.flip();
                    sc.write(byteBuffer);
                }
                // connectable
                else if(key.isConnectable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    client.finishConnect();
                }
            }




        }


    }
}
