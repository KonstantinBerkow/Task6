package my.tasks;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ServerHandler implements Runnable {

    private volatile ServerSocket serverSocket;
    private volatile boolean working = true;
    //private Thread innerThread;
    private int SECRET_KEY;
    private Vector<Thread> threads = new Vector<Thread>();

    public ServerHandler(ServerSocket serverSocket, int SECRET_KEY) {
        this.serverSocket = serverSocket;
        this.SECRET_KEY = SECRET_KEY;
        /*this.innerThread = new Thread(this);
        this.innerThread.start();*/
    }

    @Override
    public void run() {
        System.out.println("New server: " + serverSocket);
        System.out.println("Servers directory: " + ServerProgram.getServerDir());

        while (working) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection : " + clientSocket);

                Thread t = new Thread(new ClientHandler(clientSocket));
                threads.add(t);
                t.start();
            } catch (Exception e) {
                System.err.println("Error in connection attempt.");
            }
        }
    }

    public void stop(int key) {
        if (key == SECRET_KEY) {
            working = false;
            for (Thread t : threads) {
                t.interrupt();
            }
        }
    }

    public boolean isWorking() {
        return working;
    }
}
