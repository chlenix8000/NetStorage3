package com.example.netstorage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;




public class Server {

    private static Connection connection;
    private static Statement statement;

    public static void main(String[] args) throws IOException {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            statement = connection.createStatement();
        }
        catch (SQLException e) {
            log("ошибка " + e.getMessage());}


            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new ServerHandler());
                            }
                        });
                System.out.println("Сервер запущен");
                ChannelFuture f = b.bind(45001).sync();
                                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }





//        ServerSocketChannel serverChannel = ServerSocketChannel.open();
//        serverChannel.socket().bind(new InetSocketAddress(45001));
//        serverChannel.configureBlocking(false);
//        Selector selector = Selector.open();
//        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
//        log("Сервер стартовал на порту 45001. Ожидаем соединения...");
//
//        while (true) {
//            selector.select(); // Блокирующий вызов, только один
//            for (SelectionKey event : selector.selectedKeys()) {
//                if (event.isValid()) {
//                    try {
//                        if (event.isAcceptable()) { // Новое соединение
//                            SocketChannel socketChannel = serverChannel.accept(); // Не блокирующий
//                            socketChannel.configureBlocking(false);
//                            log("Подключен " + socketChannel.getRemoteAddress());
//                            socketChannel.register(selector, SelectionKey.OP_READ);
//                        } else if (event.isReadable()) { // Готов к чтению
//                            SocketChannel socketChannel = (SocketChannel) event.channel();
//                            handleReadable(socketChannel);
//                        }
//                    } catch (IOException e) {
//                        log("ошибка " + e.getMessage());
//                    }
//                }
//            }
//            selector.selectedKeys().clear();
//        }
    }
//
//    private static void handleReadable(SocketChannel socketChannel) throws IOException {
//        ConnectionMetadata connectionMetadata = socketMetadataMap.get(socketChannel);
//        if (connectionMetadata == null) {
//            connectionMetadata = new ConnectionMetadata();
//            socketMetadataMap.put(socketChannel, connectionMetadata);
//        }
//
//        ByteBuffer inboundBuffer = ByteBuffer.allocate(4096);
//        int readBytes;
//        FileChannel fileChannel = null;
//        while ((readBytes = socketChannel.read(inboundBuffer)) > 0) {
//            inboundBuffer.flip();
//            if (!connectionMetadata.isMetadataLoaded()) {
//                loadMetadata(connectionMetadata, inboundBuffer);
//            }
//            if (inboundBuffer.hasRemaining()) {
//                fileChannel = getFileChannel(socketChannel);
//                fileChannel.write(inboundBuffer);
//                inboundBuffer.clear();
//            }
//        }
//
//        if (readBytes == -1) {
//            if (fileChannel != null) {
//                fileChannel.close();
//            }
//            socketChannel.close();
//            socketFileChannel.remove(socketChannel);
//            socketMetadataMap.remove(socketChannel);
//        }
//    }
//
//    private static void loadMetadata(ConnectionMetadata connectionMetadata, ByteBuffer inboundBuffer) {
//        while (inboundBuffer.hasRemaining()) { //если в буфере еще есть данные для чтения
//            byte nextByte = inboundBuffer.get();
//            if (nextByte == ' ') { // метаинформация закончилась, дальше файл
//                connectionMetadata.buildMetadata();
//                break;
//            } else {
//                connectionMetadata.getMetadataBuffer().put(nextByte);
//            }
//        }
//    }
//
//    private static FileChannel getFileChannel(SocketChannel socketChannel) throws FileNotFoundException {
//        FileChannel fileChannel = socketFileChannel.get(socketChannel);
//        ConnectionMetadata connectionMetadata = socketMetadataMap.get(socketChannel);
//        if (fileChannel == null) {
//            Map<String, String> metadataParams = connectionMetadata.getMetadataParams();
//            String fileName = metadataParams.get("FILE_NAME");
//            RandomAccessFile fileForSend = new RandomAccessFile(SERVER_DIR + fileName, "rw");
//            fileChannel = fileForSend.getChannel();
//            socketFileChannel.put(socketChannel, fileChannel);
//        }
//        return fileChannel;
//    }

    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}


