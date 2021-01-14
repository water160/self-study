package com.basic.nio;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * 一、缓冲区在Java NIO中负责数据存取、缓冲区就是数组，用于存储不同数据类型的数据
 * 根据不同数据类型（boolean除外），提供了相应类型的缓冲区：
 * ByteBuffer（最常用）
 * CharBuffer
 * ShortBuffer
 * IntBuffer
 * LongBuffer
 * FloatBUffer
 * DoubleBuffer
 * 上述缓冲区管理方式几乎一致，都是通过allocate()获取缓冲区
 *
 * 二、缓冲区的核心方法有两个
 * put(): 存入数据到缓冲区
 * get(): 获取缓冲区中的数据
 *
 * 三、缓冲区中的四个核心属性
 * capacity: 容量，最大存储数据的容量，一旦声明无法改变
 * limit: 限制，缓冲区中可以操作数据的大小，limit后面的数据无法读写
 * position: 位置，缓冲区中正在操作数据的位置
 *
 * mark: 标记，记录position的值，可以通过reset()恢复到mark的位置
 *
 * 0 <= mark <= position <= limit <= capacity
 *
 * 五、直接缓冲区与非直接缓冲区
 * 非直接缓冲区：通过allocate()方法分配缓冲区，建立在JVM的内存中
 * 直接缓冲区：通过allocateDirect()方法分配缓冲区，建立在操作系统的物理内存中（可以提高效率）
 */
public class TestBuffer {

    @Test
    public void test3() {
        //分配直接缓冲区
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(1024);
        ByteBuffer buffer2 = ByteBuffer.allocate(1024);
        System.out.println(buffer1.isDirect());
        System.out.println(buffer2.isDirect());
    }

    @Test
    public void test2() {
        String str = "abcde";

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        buffer.put(str.getBytes());
        buffer.flip();
        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst, 0, 2);
        System.out.println(new String(dst, 0, 2));
        System.out.println(buffer.position());

        buffer.mark();

        buffer.get(dst, 2, 2);
        System.out.println(new String(dst, 2, 2));

        buffer.reset();
        System.out.println(buffer.position());

        //缓冲区中是否还有可以操作的数据，如果有查看还剩多少byte
        if (buffer.hasRemaining()) {
            System.out.println(buffer.remaining());
        }
    }

    @Test
    public void test1() {
        //1.分配一个指定大小的缓冲区，单位byte
        //position=0, limit=1024, capacity=1024
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        System.out.println("========== allocate ==========");
        System.out.println(buffer.position() + ", " + buffer.limit() + ", " + buffer.capacity());

        //2.利用put()存入数据到缓冲区
        String str = "abcde";
        buffer.put(str.getBytes());
        System.out.println("========== put ==========");
        System.out.println(buffer.position() + ", " + buffer.limit() + ", " + buffer.capacity());

        //3.利用flip()切换成读模式
        buffer.flip();
        System.out.println("========== flip ==========");
        System.out.println(buffer.position() + ", " + buffer.limit() + ", " + buffer.capacity());

        //4.利用get()获取缓冲区数据，get获取时position会变化
        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst);
        System.out.println("========== get ==========");
        System.out.println(new String(dst, 0, dst.length));
        System.out.println(buffer.position() + ", " + buffer.limit() + ", " + buffer.capacity());

        //5.rewind(): 可重复读
        buffer.rewind();
        System.out.println("========== rewind ==========");
        System.out.println(buffer.position() + ", " + buffer.limit() + ", " + buffer.capacity());

        //6.clear(): 清空缓冲区，但是缓冲区中的数据依然存在，处于“被遗忘（初始）”状态
        buffer.clear();
        System.out.println("========== clear ==========");
        System.out.println(buffer.position() + ", " + buffer.limit() + ", " + buffer.capacity());
        System.out.println((char) buffer.get());

    }
}
