package com.basic.nio;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;

/**
 * 一、通道（Channel），用于源节点与目标节点的连接，在Java NIO中负责缓冲区数据的传输。Channel本身不存储数据，因此需要配合缓冲区进行传输
 *      1.1 cpu将io接口转交给dma（和内存通信），cpu可以处理其他事件，提升利用率
 *      1.2 dma演变为通道channel（理解为独立的处理器cpu）
 *
 * 二、通道的主要实现类
 * java.nio.channels.Channel接口
 *      |-- FileChannel
 *      |-- SocketChannel
 *      |-- ServerSocketChannel
 *      |-- DatagramChannel
 *
 * 三、如何获取通道
 *      3.1 Java针对支持通道的类提供了getChannel()方法
 *      本地IO：
 *      FileInputStream/FileOutputStream
 *      RandomAccessFile
 *
 *      网络IO：
 *      Socket
 *      ServerSocket
 *      DatagramSocket
 *
 *      3.2 在JDK 1.7中的NIO.2 针对各个通道提供了静态方法open()
 *      3.3 JDK 1.7中的NIO.2 Files工具类的newByteChannel()
 *
 * 四、通道之间传输数据
 *      transferFrom()
 *      transferTo()
 *
 * 五、分散（Scatter）与聚集（Gather）
 *      分散读取（Scattering Reads）：将通道中的数据分散到多个缓冲区中，依次填满多个缓冲区
 *      聚集写入（Gathering Writes）：将多个Buffer中的数据聚集到通道中，写入position到limit之间的数据
 */
public class TestChannel {

    public static final String picPath1 = System.getProperty("user.dir") + "/src/test/resources/1.jpg";

    public static final String picPath2 = System.getProperty("user.dir") + "/src/test/resources/2.jpg";

    public static final String picPath3 = System.getProperty("user.dir") + "/src/test/resources/3.jpg";

    public static final String txtPath1 = System.getProperty("user.dir") + "/src/test/resources/1.txt";

    public static final String txtPath2 = System.getProperty("user.dir") + "/src/test/resources/2.txt";

    // 四、分散聚集
    @Test
    public void test4() {
        try {
            RandomAccessFile raf1 = new RandomAccessFile(txtPath1, "rw");

            //1.获取通道
            FileChannel channel1 = raf1.getChannel();

            //2.分配指定大小的缓冲区
            ByteBuffer buffer1 = ByteBuffer.allocate(100);
            ByteBuffer buffer2 = ByteBuffer.allocate(1024);

            //3.分散读取
            ByteBuffer[] buffers = {buffer1, buffer2};
            channel1.read(buffers);

            // flip()切换成读模式
            for (ByteBuffer buffer : buffers) {
                buffer.flip();
            }

            System.out.println(new String(buffers[0].array(), 0, buffers[0].limit()));
            System.out.println("------");
            System.out.println(new String(buffers[1].array(), 0, buffers[1].limit()));

            //4.聚集写入
            RandomAccessFile raf2 = new RandomAccessFile(txtPath2, "rw");
            FileChannel channel2 = raf2.getChannel();
            channel2.write(buffers);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 三、通道之间的数据传输（直接缓冲区）
    @Test
    public void test3() {
        long start = System.currentTimeMillis();
        try {
            FileChannel inChannel = FileChannel.open(Paths.get(picPath1), StandardOpenOption.READ);
            //CREATE_NEW 如果有会报错，如果没有会新建；CREATE 无论有或没有都新建
            FileChannel outChannel = FileChannel.open(Paths.get(picPath3), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);

            inChannel.transferTo(0, inChannel.size(), outChannel);
            inChannel.close();
            outChannel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start) + "ms");
    }


    // 二、利用通道完成文件的复制（直接缓冲区）内存映射文件方式
    //有风险：可能已经拷贝结束，但是程序还没结束，由于大文件GC，程序不够稳定，建议将直接缓冲区分配给长期驻留物理内存的文件
    @Test
    public void test2() {
        try {
            FileChannel inChannel = FileChannel.open(Paths.get(picPath1), StandardOpenOption.READ);
            //CREATE_NEW 如果有会报错，如果没有会新建；CREATE 无论有或没有都新建
            FileChannel outChannel = FileChannel.open(Paths.get(picPath3), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);

            //内存映射文件（缓冲区在物理内存中）
            MappedByteBuffer inMappedBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            //outChannel和outMappedBuffer支持的读写模式要一致
            MappedByteBuffer outMappedBuffer = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

            //直接对缓冲区进行数据读写操作
            byte[] dst = new byte[inMappedBuffer.limit()];
            //将inMappedBuffer数据获取到dst中
            inMappedBuffer.get(dst);
            //将dst数据放入outMappedBuffer中
            outMappedBuffer.put(dst);

            inChannel.close();
            outChannel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 一、利用通道完成文件的复制（非直接缓冲区）
    @Test
    public void test1() {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        //将1.jpg复制为2.jpg
        try {
            fis = new FileInputStream(picPath1);
            fos = new FileOutputStream(picPath2);
            System.out.println("=== new FileInputStream FileOutputStream ===");
            //1.获取通道
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();
            System.out.println("=== get Channel ===");

            //2.分配指定大小的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //3.将通道中的数据存入缓冲区
            while (inChannel.read(buffer) != -1) {
                //如果通道读取数据不为空，将缓冲区数据写入通道
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
           // outChannel.close();
           // inChannel.close();
           // fos.close();
           // fis.close();
            if (outChannel != null ) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
