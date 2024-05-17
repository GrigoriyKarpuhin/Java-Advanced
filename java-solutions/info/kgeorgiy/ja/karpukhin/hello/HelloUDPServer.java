package info.kgeorgiy.ja.karpukhin.hello;

import info.kgeorgiy.java.advanced.hello.NewHelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of {@link NewHelloServer} interface using UDP protocol.
 */
public class HelloUDPServer implements NewHelloServer {

    private ExecutorService workers;
    private final List<DatagramSocket> sockets = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(int threads, Map<Integer, String> ports) {
        workers = Executors.newFixedThreadPool(threads + ports.size());
        for (Map.Entry<Integer, String> entry : ports.entrySet()) {
            try {
                DatagramSocket socket = new DatagramSocket(entry.getKey());
                sockets.add(socket);
                socket.setSoTimeout(100);
                byte[] buffer = new byte[socket.getReceiveBufferSize()];
                workers.execute(() -> {
                    while (!Thread.interrupted() && !socket.isClosed()) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        try {
                            socket.receive(packet);
                            String request = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                            String response = entry.getValue().replace("$", request);
                            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                            socket.send(responsePacket);
                        } catch (IOException e) {
                            System.err.println("Failed to receive or send packet: " + e.getMessage());
                        }
                    }
                });
            } catch (SocketException e) {
                System.err.println("Failed to create socket: " + e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        sockets.forEach(DatagramSocket::close);
        workers.close();
    }

    /**
     * Main method for running the server.
     * @param args - command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: HelloUDPServer <port number> <number of worker threads>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);

        try (HelloUDPServer server = new HelloUDPServer()) {
            server.start(threads, Map.of(port, "Hello, $"));
        } catch (Exception e) {
            System.err.println("Error while running the server: " + e.getMessage());
        }
    }
}