package org.orson.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * Called when the channel it's active and writable
     * 等于事件 OP_CONNECT，当active时候，客户端就是writable，可以使用ChannelHandlerContext进行输出消息
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 等同于事件 OP_READ
     * 1）客户端SocketChannel在active之后，变成isConnectable
     * 2）服务器收到事件OP_CONNECT，建立连接
     * 3）服务器建立连接后，客户端变成isWritable，发送了"Netty rocks!"消息
     * 4）服务器变成isReadable，接受消息，并且返回另外当消息
     * 5）客户端变成isReadable，接受消息打印
     * @param channelHandlerContext
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        System.out.println("Client received: " + byteBuf.toString(CharsetUtil.UTF_8));
    }
}
