package Client;

import java.io.IOException;
import java.net.*;

/**
 * Created by johanpettersson on 07/09/15.
 */
public class Client {

    public static void main(String[] args) {
        DatagramSocket aSocket = null;
        int serverPort = 50115;

        try {
            aSocket = new DatagramSocket();
            InetAddress aHost = InetAddress.getLocalHost();

            byte[] m = "Hello world!".getBytes();

            DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);

            System.out.println("Reply: " + new String(reply.getData(), 0, reply.getLength()));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
