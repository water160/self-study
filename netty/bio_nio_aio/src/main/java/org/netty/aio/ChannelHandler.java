package org.netty.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;

public class ChannelHandler {

    private AsynchronousSocketChannel socketChannel;
    private Charset charset;

    public ChannelHandler(AsynchronousSocketChannel socketChannel, Charset charset) {
        this.socketChannel = socketChannel;
        this.charset = charset;
    }

    /*
     * 使用
     * @param msg
     */
    public void writeAndFlush(Object msg) {
        byte[] bytes = msg.toString().getBytes(charset);
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        //flip将缓冲区进行读写模式切换
        writeBuffer.flip();
        socketChannel.write(writeBuffer);
    }

    public AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
