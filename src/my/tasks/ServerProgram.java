package my.tasks;

import java.io.*;
import java.net.ServerSocket;

public class ServerProgram {

    //private static volatile ServerSocket serverSocket;
    private static volatile File serverDir;
    /*private static volatile boolean working;
    private static int SECRET_KEY;*/

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        serverDir = new File(args[1]);
        int SECRET_KEY = Integer.parseInt(args[2]);

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started.");

            ServerHandler server = new ServerHandler(serverSocket, SECRET_KEY);
            Thread st = new Thread(server);
            st.start();

            BufferedReader localInput = new BufferedReader(new InputStreamReader(System.in));

            while (server.isWorking()) {
                String inputLine = localInput.readLine();
                if (inputLine.equals("stop")) {
                    server.stop(Integer.parseInt(localInput.readLine()));
                    if (SECRET_KEY == Integer.parseInt(localInput.readLine())) {
                        System.out.println(server.isWorking());
                        return;
                    }
                }
            }

            localInput.close();
        } catch (Exception e) {
            System.err.println("Port already in use.");
            System.exit(1);
        }
    }

    public static File getServerDir() {
        return serverDir;
    }
}
