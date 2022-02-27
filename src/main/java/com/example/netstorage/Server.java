package com.example.netstorage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;


public class Server {

    private static Connection connection;
    private static Statement statement;
    ExecutorService executorService = Executors.newFixedThreadPool(5);


    public static void main(String[] args) throws IOException {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            statement = connection.createStatement();
        } catch (SQLException e) {
            log("ошибка " + e.getMessage());
        }


        Thread authThread = new Thread(new Runnable() {
            @Override
            public void run() {
        try (ServerSocket serverSocket = new ServerSocket(45002)) {
            while (true) {
                System.out.println("Сервер авторизации ожидает подключения");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился к серверу авторизации");
                new AuthHandler(new Server() , socket);
            }
        } catch (IOException e) {
            System.out.println("Ошибка в работе сервера авторизации");
        }
            }});
        authThread.start();

//        Thread dataThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
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
            System.out.println("Сервер передачи данных запущен");
            ChannelFuture f = b.bind(45001).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
//            }});
//        dataThread.start();

    }

    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}


