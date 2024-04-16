package Server;

import java.net.*;
import java.io.*;

/**
 * This is the server class for the network socket based connect 4 game.
 * It simply accepts to client connections and splits them off into their
 * own game thread. Obviously the server needs to be run before trying to connect
 * a client.
 *
 * This is essentially entirely Earl Foxwells code provided on our class brightspace resource page.
 *
 */

public class Server {
    public static void main(String[] args) {
        int portNumber = 1024;
        try (
                ServerSocket serverSocket = new ServerSocket(portNumber); //Initializes Server Socket @ specified portNumber
        ) {
            while (true) { //Continously accepts new connections and pairs up two connections.
                Socket client1=serverSocket.accept();
                Socket client2=serverSocket.accept();
                new GameThread(client1,client2).start(); //Splits the two clients off into their own game thread.
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
