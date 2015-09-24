package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by johanpettersson on 07/09/15.
 */
public class Server {
    public static void main(String[] args) {
        int serverPort = 50120;
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(serverPort);
            byte[] buffer = new byte[1000];

            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(request);
            String str = new String(request.getData(), 0, request.getLength());
            if(str.equals("hello")) {
                System.out.println("Got: " + new String(request.getData(), 0, request.getLength()));
                String okMsg = "OK";
                DatagramPacket okDatagramPacket = new DatagramPacket(okMsg.getBytes(), okMsg.getBytes().length, request.getAddress(), request.getPort());
                aSocket.send(okDatagramPacket);
                System.out.println("Sent: " + new String(okDatagramPacket.getData(), 0, okDatagramPacket.getLength()));
            }
            else {
                System.out.println("Didn't get hello");
                String errorMsg = "FATAL ERROR Received: ".concat(str);
                System.out.println(errorMsg);
                aSocket.send(new DatagramPacket(errorMsg.getBytes(), errorMsg.length(), request.getAddress(), request.getPort()));
            }
            System.out.println("Connection successfully established");

            //TODO move game logic to a separate method
            GuessMyNumber myGame = new GuessMyNumber();
            while (true) {
                buffer = new byte[1000];
                request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String guess = new String(request.getData(), 0, request.getLength());

                if(guess.contains("FIN"))
                    break;

                System.out.println("Got: " + guess);

                int answer = Integer.parseInt(new String(request.getData(), 0, request.getLength()));

                buffer = myGame.compare(answer).getBytes();
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
                aSocket.send(reply);
                System.out.println("Sent: " + new String(reply.getData(), 0, request.getLength()));
            }
            //TODO Fin handshake?
            System.out.println("Game over! Halting execution.");
        } catch (SocketException e) { System.err.println("Socket: " + e.getMessage());
        } catch (IOException e) { System.err.println("IO: " + e.getMessage());
        }finally {
            aSocket.close();
        }
    }
}