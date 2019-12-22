package org.orson.io.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {

    public static void main(String[] args) throws Exception{
        //server 1
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        // configured as non-blocking
        socketChannel.configureBlocking(false);

        //create a socket
        socketChannel.bind(new InetSocketAddress(9090));

        // create a selector
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //buff
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        for(;;) {

            try{
                selector.select();
            }catch (IOException e) {
                e.printStackTrace();
                break;
            }

            // get the selected keys
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            while(iterator.hasNext()) {
                // get and remove the key from iterator
                SelectionKey key = iterator.next();
                iterator.remove();

                try {
                    if(key.isAcceptable()) {
                        // return the registered channel on this selector & event
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        System.out.println("welcome a client channel...");
                        // tell selector if client is writable, tell client if readable
                        client.register(selector, SelectionKey.OP_READ);
                    }
                    else if(key.isReadable()) {
                        System.out.println("Server invoke readable.");

                        SocketChannel channel = (SocketChannel) key.channel();

                        int read = channel.read(buffer);
                        buffer.flip();

                        System.out.print(new String(buffer.array(), 0, read, Charset.forName("UTF-8")));


                        buffer.clear();

                        buffer.put(ByteBuffer.wrap("Hello Orson, this is server response.".getBytes(Charset.forName("UTF-8"))));
                        buffer.flip();
                        channel.write(buffer);
                    }


                }catch(IOException e) {
                    key.cancel();
                    try {
                        key.channel().close();
                    }catch(IOException e0) {
                        //skip
                    }
                }

            }

        }

    }
}
