package Client;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

/**
 * Created by johanpettersson on 07/09/15.
 */
public class Client {
    private static DatagramSocket aSocket = null;
    private static InetAddress aHost = null;
    private static int serverPort = 50120;
    private static DatagramPacket reply = null;

    public static void main(String[] args) {
        try {
            aSocket = new DatagramSocket();
            aSocket.setSoTimeout(5000);
            aHost = InetAddress.getLocalHost();

            DatagramPacket request = newSendPacket("hello", aHost, serverPort);
            aSocket.send(request);
            System.out.println("Sent: " + new String(request.getData(), 0, request.getLength()));

            reply = newReceivePacket();
            aSocket.receive(reply);
            System.out.println("Got: " + new String(reply.getData(), 0, reply.getLength()));

            System.out.println("Connection successfully established");

            DatagramPacket startRequest = newSendPacket("start", aHost, serverPort);
            aSocket.send(startRequest);
            System.out.println("Sent: " + new String(startRequest.getData(), 0, startRequest.getLength()));

            playGame();
        }
        catch (SocketTimeoutException ste) {
            System.err.println("Timeout!");
        }
        catch (UnknownHostException e) {
            System.err.println("IP: " + e.getMessage());
        }
        catch (SocketException e) {
            System.err.println("Socket: " + e.getMessage());
        }
        catch (IOException e) {
            System.err.println("IO: " + e.getMessage());
        }
        finally {
            aSocket.close();
        }
    }

    private static void playGame() {
        try {
            Scanner scan;
            String guess;
            String state = "";
            while(!state.contains("CORRECT")) {
                scan = new Scanner(System.in);
                System.out.print("Guess: ");
                guess = scan.nextLine();
                System.out.println(guess);
                aSocket.send(new DatagramPacket(guess.getBytes(), guess.length(), aHost, serverPort));

                reply = newReceivePacket();
                aSocket.receive(reply);

                state = new String(reply.getData(), 0, reply.getLength());
                System.out.println("Reply: " + state);
            }
            String finMsg = "FIN";
            DatagramPacket finDatagramPacket = new DatagramPacket(finMsg.getBytes(), finMsg.length(), aHost, serverPort);
            aSocket.send(finDatagramPacket);
            System.out.println("Sent: " + new String(finDatagramPacket.getData(), 0, finDatagramPacket.getLength()));
            System.out.println("You won! Halting execution.");
        }
        catch (IOException ioe) {
            System.out.println("IO Exception.");
        }
    }

    private static DatagramPacket newSendPacket(String text, InetAddress address, int port) {
        byte[] data = text.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);

        return packet;
    }

    private static DatagramPacket newReceivePacket() {
        byte[] data = new byte[4096];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        return packet;
    }
}
