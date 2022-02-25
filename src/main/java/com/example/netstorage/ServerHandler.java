package com.example.netstorage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент подключен");
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(new String("Введите логин и пароль").getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(byteBuf);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buffer = (ByteBuf) msg;
        while (buffer.readableBytes()>0){
            System.out.println(buffer);
        }
        buffer.release();

    }
}
