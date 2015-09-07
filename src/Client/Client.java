package Client;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

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

            byte[] helloMsg = "hello".getBytes();

            DatagramPacket request = new DatagramPacket(helloMsg, helloMsg.length, aHost, serverPort);
            aSocket.send(request);

            System.out.println("Sent: " + new String(request.getData(), 0, request.getLength()));

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);

            System.out.println("Got: " + new String(reply.getData(), 0, reply.getLength()));

            System.out.println("Connection successfully established");

            //TODO move game logic to a separate method
            Scanner scan;
            String guess;
            String state = "";
            while(!state.contains("CORRECT")) {
                scan = new Scanner(System.in);
                System.out.print("Guess: ");
                guess = scan.nextLine();
                aSocket.send(new DatagramPacket(guess.getBytes(), guess.length(), aHost, serverPort));

                buffer = new byte[1000];
                reply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply);

                state = new String(reply.getData(), 0, reply.getLength());
                System.out.println("Reply: " + state);
            }
            String finMsg = "FIN";
            DatagramPacket finDatagramPacket = new DatagramPacket(finMsg.getBytes(), finMsg.length(), aHost, serverPort);
            aSocket.send(finDatagramPacket);
            System.out.println("Sent: " + new String(finDatagramPacket.getData(), 0, finDatagramPacket.getLength()));
            System.out.println("You won! Halting execution.");

        } catch (UnknownHostException e) { System.err.println("IP: " + e.getMessage());
        } catch (SocketException e) { System.err.println("Socket: " + e.getMessage());
        } catch (IOException e) { System.err.println("IO: " + e.getMessage());
        }finally {
            aSocket.close();
        }
    }
}
