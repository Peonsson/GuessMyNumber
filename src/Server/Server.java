package Server;

import java.io.IOException;
import java.net.*;

/**
 * Created by Peonsson and roppe546 on 07/09/15.
 */
public class Server {
    private static DatagramSocket aSocket = null;
    private static byte[] buffer = null;
    private static DatagramPacket request = null;
    private static DatagramPacket okDatagramPacket = null;
    private static DatagramPacket startRequest = null;

    public static void main(String[] args) {
        int numOfRetries = 0;
        int serverPort = 50120;

        try {
            aSocket = new DatagramSocket(serverPort);

            while (true) {
                System.out.println("waiting for connection...");
                request = newReceivePacket();

                aSocket.receive(request);

                String str = new String(request.getData(), 0, request.getLength());
                System.out.println(str);
                if (str.equals("hello")) {
                    System.out.println("Got: " + new String(request.getData(), 0, request.getLength()));
                    okDatagramPacket = newSendPacket("OK", request.getAddress(), request.getPort());
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

                //in production
                startRequest = newReceivePacket();

                try {
                    aSocket.setSoTimeout(5000);
                    aSocket.receive(startRequest);
                }
                catch (SocketTimeoutException ste) {
                    System.err.println("Timed out waiting for start.");
                    continue;
                }

                str = new String(startRequest.getData(), 0, startRequest.getLength());

                if(str.toLowerCase().equals("start")) {
                    playGame();
                } else {
                    System.out.println("Didn't get start");
                    String errorMsg = "FATAL ERROR Received: ".concat(str);
                    System.out.println(errorMsg);
                    aSocket.send(new DatagramPacket(errorMsg.getBytes(), errorMsg.length(), request.getAddress(), request.getPort()));
                }

                //TODO Fin handshake?
                System.out.println("Game over!");
                aSocket.setSoTimeout(0);
            }
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
        GuessMyNumber myGame = new GuessMyNumber();

        while (true) {
            try {
                request = newReceivePacket();

                try {
                    aSocket.setSoTimeout(60000);
                    aSocket.receive(request);
                } catch (SocketTimeoutException ste) {
                    System.out.println("Timed out while waiting for client.");
                    return;
                }

                String guess = new String(request.getData(), 0, request.getLength());

                if (guess.contains("FIN"))
                    break;

                System.out.println("Got: " + guess);
                int answer = 0;

                try {
                    answer = Integer.parseInt(new String(request.getData(), 0, request.getLength()));
                } catch (NumberFormatException e){
                    aSocket.send(newSendPacket("Please use numbers only.", request.getAddress(), request.getPort()));
                    continue;
                }

                DatagramPacket reply = newSendPacket(myGame.compare(answer), request.getAddress(), request.getPort());
                aSocket.send(reply);
                System.out.println("Sent: " + new String(reply.getData(), 0, reply.getLength()));
            }
            catch (SocketException e) {
                System.err.println("Socket: " + e.getMessage());
            }
            catch (IOException e) {
                System.err.println("IO: " + e.getMessage());
            }
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