package Server;

import sun.security.x509.IPAddressName;

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
    private static InetAddress currentClientIP = null;
    private static int currentClientPort;

    public static void main(String[] args) {

        int serverPort = 50120;
        try {
            aSocket = new DatagramSocket(serverPort);

            while (true) {
                System.out.println("Waiting for connection...");
                request = newReceivePacket();
                aSocket.receive(request);
                currentClientIP = request.getAddress();
                currentClientPort = request.getPort();

                String str = new String(request.getData(), 0, request.getLength());
                if (str.equals("hello")) {
                    System.out.println("RECV: " + new String(request.getData(), 0, request.getLength()));

                    okDatagramPacket = newSendPacket("OK", request.getAddress(), request.getPort());
                    aSocket.send(okDatagramPacket);
                    System.out.println("SENT: OK");
                }
                else {
                    System.out.println("RECV: " + new String(request.getData(), 0, request.getLength()));

                    DatagramPacket HelloErrorDatagram = newSendPacket("ERROR", request.getAddress(), request.getPort());
                    aSocket.send(HelloErrorDatagram);
                    System.out.println("SENT: ERROR");

                    continue;
                }
                System.out.println("Server: Connection successfully established");

                startRequest = newReceivePacket();

                try {
                    aSocket.setSoTimeout(10000);
                    aSocket.receive(startRequest);
                }
                catch (SocketTimeoutException ste) {
                    System.err.println("Timed out waiting for start.");
                    continue;
                }

                str = new String(startRequest.getData(), 0, startRequest.getLength());
                if(str.equals("start")) {
                    System.out.println("RECV: " + new String(startRequest.getData(), 0, startRequest.getLength()));

                    DatagramPacket gameStartedDatagram = newSendPacket("STARTED", startRequest.getAddress(), startRequest.getPort());
                    aSocket.send(gameStartedDatagram);
                    System.out.println("SENT: STARTED");

                    playGame();
                } else {
                    System.out.println("RECV: " + new String(startRequest.getData(), 0, startRequest.getLength()));

                    DatagramPacket startErrorDatagram = newSendPacket("ERROR", startRequest.getAddress(), startRequest.getPort());
                    aSocket.send(startErrorDatagram);
                    System.out.println("SENT: ERROR");

                    aSocket.setSoTimeout(0);
                    continue;
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
        int answer = -1;

        while (true) {
            try {
                request = newReceivePacket();
                try {
                    aSocket.setSoTimeout(60000);
                    aSocket.receive(request);

                    if(!(request.getAddress().equals(currentClientIP)) || request.getPort() != currentClientPort) {
                        aSocket.send(newSendPacket("BUSY", request.getAddress(), request.getPort()));
                        continue;
                    }

                } catch (SocketTimeoutException ste) {
                    System.out.println("Timed out while waiting for client.");
                    return;
                }

                String guess = new String(request.getData(), 0, request.getLength());

                if (guess.contains("fin")) {
                    aSocket.send(newSendPacket("CANCELED", request.getAddress(), request.getPort()));
                    break;
                }

                System.out.println("Got: " + guess);
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