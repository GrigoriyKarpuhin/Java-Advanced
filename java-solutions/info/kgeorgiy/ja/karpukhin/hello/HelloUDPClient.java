package info.kgeorgiy.ja.karpukhin.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of {@link HelloClient} interface using UDP protocol.
 */
public class HelloUDPClient implements HelloClient {

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        ExecutorService workers = Executors.newFixedThreadPool(threads);
        for (int i = 1; i <= threads; i++) {
            final int threadId = i;
            workers.execute(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(100);
                    byte[] buffer = new byte[socket.getReceiveBufferSize()];
                    for (int j = 1; j <= requests; j++) {
                        String request = prefix + threadId + "_" + j;
                        byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, InetAddress.getByName(host), port);
                        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                        while (!Thread.interrupted() && !socket.isClosed()) {
                            try {
                                socket.send(requestPacket);
                                socket.receive(responsePacket);
                                String response = new String(responsePacket.getData(), responsePacket.getOffset(),
                                        responsePacket.getLength(), StandardCharsets.UTF_8);
                                if (response.contains(request)) {
                                    break;
                                }
                            } catch (IOException e) {
                                System.err.println("Failed to send or receive packet: " + e.getMessage());
                            }
                        }
                    }
                } catch (SocketException e) {
                    System.err.println("Failed to create socket: " + e.getMessage());
                } catch (UnknownHostException e) {
                    System.err.println("Failed to resolve host: " + e.getMessage());
                }
            });
        }
        workers.close();
    }

    /**
     * Main method for running the client.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Usage: HelloUDPClient <host> <port> <prefix> <threads> <requests>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String prefix = args[2];
        int threads = Integer.parseInt(args[3]);
        int requests = Integer.parseInt(args[4]);

        try {
            HelloUDPClient client = new HelloUDPClient();
            client.run(host, port, prefix, threads, requests);
        } catch (Exception e) {
            System.err.println("Error while running the client: " + e.getMessage());
        }
    }
}