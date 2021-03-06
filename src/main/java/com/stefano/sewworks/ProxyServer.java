package com.stefano.sewworks;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import com.stefano.sewworks.pipe.ListenableMessagePipe;
import com.stefano.sewworks.stats.StatsCollector;

/**
 * Author stefanofranz
 */
public class ProxyServer {

    private final String listenAddress;
    private final int listenPort;
    private final String forwardAddress;
    private final int forwardPort;
    private final StatsCollector statsCollector;

    public ProxyServer(String listenAddress, int listenPort, String forwardAddress, int forwardPort, StatsCollector statsCollector) {
        this.listenAddress = listenAddress;
        this.listenPort = listenPort;
        this.forwardAddress = forwardAddress;
        this.forwardPort = forwardPort;
        this.statsCollector = statsCollector;
    }


    public void startAndWait() throws ExecutionException, InterruptedException, IOException {
        final AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(
                AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool()))
                .bind(new InetSocketAddress(listenAddress, listenPort));

        while (true) {
            AsynchronousSocketChannel socket = server.accept().get();
            System.err.println("connection from: " + socket.getRemoteAddress().toString());
            ListenableMessagePipe clientMultiplexor = new ListenableMessagePipe(socket,
                    forwardAddress,
                    forwardPort,
                    statsCollector,
                    "\n");
            clientMultiplexor.startPipe();
        }

    }

    public void dumpToOutput() {
        statsCollector.scheduleStatsDump();
    }
}
