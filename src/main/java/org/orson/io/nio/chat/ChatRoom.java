package org.orson.io.nio.chat;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;

public class ChatRoom {

    private String name;

    private Selector selector = null;


    private List<SocketChannel> attenders = null;

    public ChatRoom(String name) {
        this.name = name;

        if(Objects.isNull(attenders)) {
            attenders = new ArrayList<>();
        }

        if(Objects.isNull(selector)) {
            try {
                selector = Selector.open();
            }catch (IOException e) {
                // skip, ignore
            }
        }
    }

    public void open() {

        try {
            // open a server socket channel
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(9999));

            //listen and accept client socket channel
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        }catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        for (;;) {

            try {
                selector.select();
            }catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if(key.isAcceptable()) {
                    ServerSocketChannel sc = (ServerSocketChannel) key.channel();
                    try{
                        SocketChannel client = sc.accept();
                        System.out.println("welcome a client connected...");
                        Boolean isAccpted = this.acceptClient(client);
                        if(isAccpted) {
                            this.attenders.add(client);
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }

                }
                // readable
                else if(key.isReadable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    String clientName = (String) key.attachment();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    
                    // default msg
                    StringBuilder msg = new StringBuilder();
                    try {
                        /*
                        what will happen will the message over 1024 bytes.
                         */
                        int len = -1;
                        while (( len = sc.read(byteBuffer)) != 0) {
                            msg.append(new String(byteBuffer.array(), 0, len, Charset.forName("UTF-8")));
                        }
                    }catch (IOException e) {
                        //skip
                    }

                    // board cast
                    if(msg.length() > 0) {
                        // 因为attachment是在client的 socket上的，所里在server上获取的client的socket channel没有attachment
                        this.boardcast(clientName, msg.toString(), sc);
                    }
                    
                }

            }

        }


    }

    private void boardcast(String clientName, String msg, SocketChannel sender) {

        for(SocketChannel sc : this.attenders) {
            // skip the sender
            if(sender.equals(sc)) {
                continue;
            }

            try {
                String rs = String.format("[%s] said: %s", clientName, msg);
                sc.write(ByteBuffer.wrap(rs.getBytes(Charset.forName("UTF-8"))));

            }catch (IOException e) {
                System.out.println("socket failed to broadcast message.");
                continue;
            }
        }

    }

    private Boolean acceptClient(SocketChannel sc) {
        try {
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }catch (IOException e) {
            return false;
        }
        return true;
    }
}
