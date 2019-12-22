package org.orson.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        /*
        if(args.length != 1) {
            System.out.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
        }
        */

        int port = Integer.parseInt("9999");
        new EchoServer(port).start();
    }

    /**
     * Start a socket server
     * @throws Exception
     */
    private void start() throws Exception {
        // init the customized channel handler
        EchoServerHandler echoServerHandler = new EchoServerHandler();
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            // new sever
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group)
                    // create a channel
                    .channel(NioServerSocketChannel.class)
                    // listen on a port
                    .localAddress(new InetSocketAddress(port))
                    // append a channel handler in the pipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // add our customized channel handler
                            socketChannel.pipeline().addLast(echoServerHandler);
                        }
                    });
            // bind server and call sync to block current thread until the bind action done
            ChannelFuture future = serverBootstrap.bind().sync();
            future.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully().sync();
        }
    }
}
