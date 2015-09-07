package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
    public static void main(String[] args) {
        int serverPort = 50115;
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(serverPort);
            byte[] buffer = new byte[1000];

            // OK
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(request);
            if(request.getData().toString().toLowerCase().equals("hello"))
                aSocket.send(new DatagramPacket("OK".getBytes(),"OK".getBytes().length, request.getAddress(), request.getPort()));
            else
                aSocket.send(new DatagramPacket("ERR".getBytes(),"ERR".getBytes().length, request.getAddress(), request.getPort()));

            GuessMyNumber myGame = new GuessMyNumber();
            while (true) {
                request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                int answer = Integer.parseInt(request.getData().toString());
                int compare = myGame.compare(answer);

                DatagramPacket reply = new DatagramPacket(compare, request.getLength(), request.getAddress(), request.getPort());
                aSocket.send(reply);
            }
        } catch (SocketException e) {
            System.err.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO: " + e.getMessage());
        }
    }
}