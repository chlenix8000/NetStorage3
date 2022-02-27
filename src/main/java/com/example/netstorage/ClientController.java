package com.example.netstorage;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.Socket;

public class ClientController {
    Bootstrap bootstrap = new Bootstrap();
    SocketChannel channel;
    ClientHandler clientHandler;
    ByteBuf sendBuf;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    String fileInfo;
    String filePath;
    String loginMsg;

    @FXML
    Button logBtn;
    @FXML
    TextField log;
    @FXML
    PasswordField pass;
    @FXML
    Label ServerStatus;
    @FXML
    Label lblU;
    @FXML
    Label lblP;
    @FXML
    Label lblCu;
    @FXML
    Label lblCd;
    @FXML
    Button dwlBtn;
    @FXML
    Button delBtn;
    @FXML
    Button selBtn;
    @FXML
    Button uplBtn;

    @FXML
    public void initialize() throws IOException {
        setAuthorized(false);

        try {
            socket = new Socket("localhost", 45002);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Подключение к серверу авторизации");
            ServerStatus.setText("*Введите учетные данные");
        } catch (IOException e) {
            ServerStatus.setText("*Не удалось подключиться к серверу");
            e.printStackTrace();
        }
    }

    public void setAuthorized(boolean b) {
        if (b) {
            lblU.setVisible(false);
            lblP.setVisible(false);
            pass.setVisible(false);
            logBtn.setVisible(false);
            log.setVisible(false);
            lblCd.setVisible(true);
            lblCu.setVisible(true);
            dwlBtn.setVisible(true);
            delBtn.setVisible(true);
            selBtn.setVisible(true);
            uplBtn.setVisible(true);
        } else {
            ServerStatus.setText("Введите логин и пароль");
            lblU.setVisible(true);
            lblP.setVisible(true);
            pass.setVisible(true);
            logBtn.setVisible(true);
            log.setVisible(true);
            lblCd.setVisible(false);
            lblCu.setVisible(false);
            dwlBtn.setVisible(false);
            delBtn.setVisible(false);
            selBtn.setVisible(false);
            uplBtn.setVisible(false);
        }
    }

    public void btnLogin(ActionEvent actionEvent) throws IOException {
        setAuthorized(false);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String strFromServer = null;
                    try {
                        strFromServer = in.readUTF();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (strFromServer.startsWith("/authok")) {
                        setAuthorized(true);
                        String myNick = strFromServer.split("\\s")[1];
                        ServerStatus.setText("*Вы авторизованы как: " + myNick);
                        break;
                    }
                    else ServerStatus.setText("Неверный логин или пароль");
                }
                setAuthorized(true);

                EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
                try {
                    bootstrap.group(eventLoopGroup);
                    bootstrap.channel(NioSocketChannel.class);
                    bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) throws Exception {
                            channel = socketChannel;
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(clientHandler = new ClientHandler());
                        }
                    });
                    ChannelFuture channelFuture = bootstrap.connect("localhost", 45001).sync();
                    channelFuture.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    eventLoopGroup.shutdownGracefully();
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


            }
        });
        t.start();

        String loginMsg = new String("/auth " + log.getText() + " " + pass.getText());
        System.out.println(loginMsg);
        out.writeUTF("/auth " + log.getText() + " " + pass.getText());

        //Не получается передать данные авторизации String при помощи буффера(
//            sendBuf.alloc().buffer();
//            sendBuf.writeBytes(loginMsg.getBytes());
//            System.out.println(sendBuf);
//            channel.writeAndFlush(sendBuf);

        log.clear();
        pass.clear();
    }

    public void selBtn(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            fileInfo = selectedFile.getName();
            filePath = selectedFile.getAbsolutePath();
            System.out.println(fileInfo);
            System.out.println(filePath);
        } else System.out.println("Выберите файл");
    }

    public void uplBtn(ActionEvent actionEvent) throws IOException {
        out.writeUTF("/file " + fileInfo);
        File file = new File(filePath);
        channel.writeAndFlush(file);

    }



}