package Client;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

/**
 * Created by Peonsson and roppe546 on 07/09/15.
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

            Scanner scan = new Scanner(System.in);
            String msg = scan.nextLine();
            DatagramPacket request = newSendPacket(msg, aHost, serverPort);
            aSocket.send(request);
            System.out.println("Sent: " + msg);

            reply = newReceivePacket();
            aSocket.receive(reply);

            String receiveMsg = new String(reply.getData(), 0, reply.getLength());
            if (receiveMsg.equals("ERROR")) {
                System.err.println(receiveMsg);
                aSocket.close();
                return;
            }
            else if (receiveMsg.equals("BUSY")) {
                System.err.println(receiveMsg);
                aSocket.close();
                return;
            }

            System.out.println("Got: " + receiveMsg);

            msg = scan.nextLine();
            DatagramPacket startRequest = newSendPacket(msg, aHost, serverPort);
            aSocket.send(startRequest);
            System.out.println("Sent: " + msg);

            reply = newReceivePacket();
            aSocket.receive(reply);

            receiveMsg = new String(reply.getData(), 0, reply.getLength());
            if (receiveMsg.equals("STARTED")) {
                playGame();
            }
            else {
                System.err.println(receiveMsg);
                aSocket.close();
                return;
            }


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
        System.out.println("Playing game!");
        try {
            Scanner scan;
            String guess;
            String state = "";
            while(!state.contains("CORRECT")) {
                scan = new Scanner(System.in);
                System.out.print("Guess: ");
                guess = scan.nextLine();

                aSocket.send(new DatagramPacket(guess.getBytes(), guess.length(), aHost, serverPort));

                if (guess.equals("fin")) {
                    aSocket.close();
                    return;
                }

                reply = newReceivePacket();
                aSocket.receive(reply);

                state = new String(reply.getData(), 0, reply.getLength());
                System.out.println("Reply: " + state);
            }
            String finMsg = "fin";
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
