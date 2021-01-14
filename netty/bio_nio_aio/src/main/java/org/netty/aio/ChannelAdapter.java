package org.netty.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public abstract class ChannelAdapter implements CompletionHandler<Integer, Object> {

    private AsynchronousSocketChannel socketChannel;
    private Charset charset;

    public ChannelAdapter(AsynchronousSocketChannel socketChannel, Charset charset) {
        this.socketChannel = socketChannel;
        this.charset = charset;
    }

    public abstract void channelActive(ChannelHandler ctx);
    public abstract void channelInactive(ChannelHandler ctx);
    public abstract void channelRead(ChannelHandler ctx, Object msg);

    @Override
    public void completed(Integer result, Object attachment) {
        final ByteBuffer buffer = ByteBuffer.allocate(1024);
        final long timeout = 60 * 60L;
        socketChannel.read(buffer, timeout, TimeUnit.SECONDS, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                if (result == -1) {
                    //没获取到结果
                    channelInactive(new ChannelHandler(socketChannel, charset));
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }
        })
    }

    @Override
    public void failed(Throwable exc, Object attachment) {

    }
}
