package percolation.util;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class RemoteStream extends PrintStream {

    public static final int DEFAULT_PORT = 5033;

    public RemoteStream() throws SocketException {
        super(new RemoteOutputStream(DEFAULT_PORT), true);
    }

    public RemoteStream(String fileName) throws FileNotFoundException {
        super(new FileOutputStream(fileName), true);
    }

    public RemoteStream(OutputStream out) {
        super(out, true);
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        byte[] buf = new byte[1024];
        System.out.println("RemoteOutputStream listening on port " + port);
        try (DatagramSocket ds = new DatagramSocket(port)) {
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            while (!ds.isClosed()) {
                ds.receive(dp);
                System.out.write(dp.getData(), 0, dp.getLength());
            }
        } catch (IOException ignored) {}
    }

    public static class RemoteOutputStream extends OutputStream {

        private final DatagramSocket ds;
        private final DatagramPacket dp;

        public RemoteOutputStream(int port) throws SocketException {
            this(new InetSocketAddress("127.0.0.1", port));
        }

        public RemoteOutputStream(InetSocketAddress addr) throws SocketException {
            ds = new DatagramSocket();
            dp = new DatagramPacket(new byte[1024], 1024, addr);
        }

        @Override
        public void write(int b) throws IOException {
            byte[] data = new byte[] { (byte) b };
            dp.setData(data);
            ds.send(dp);
            flush();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            dp.setData(b, off, len);
            ds.send(dp);
            flush();
        }

        @Override
        public void close() {
            ds.close();
        }
    }
}
