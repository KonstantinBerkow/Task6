package my.tasks;

import com.sun.corba.se.spi.activation.Server;

import java.io.*;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Set;


public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private boolean working = true;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (working) {
                String input = clientInput.readLine();

                if (input.equals("disconnect")) {
                    System.out.println("Client: " + clientSocket + " has disconnected");
                    working = false;
                } else {
                    System.out.println("Client Said: " + input);
                    String[] commandArgs = SomeTools.parseArgs(input);
                    if (commandArgs[0].equals("files")) {
                        listFiles();
                    } else if (commandArgs[0].equals("download")) {
                        sendFile(commandArgs[1]);
                    } else if (commandArgs[0].equals("upload")) {
                        receiveFile(commandArgs[1]);
                    }
                }
            }

            clientInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(String serverDestination) throws IOException {
        int bytesRead;

        DataInputStream clientData = new DataInputStream(clientSocket.getInputStream());

        String fileName = clientData.readUTF();

        OutputStream output;
        if (serverDestination.lastIndexOf(File.separatorChar) != serverDestination.length() - 1)
            output = new FileOutputStream((serverDestination + File.separator + fileName));
        else output = new FileOutputStream((serverDestination + fileName));

        long size = clientData.readLong();
        byte[] buffer = new byte[1024];
        while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            output.write(buffer, 0, bytesRead);
            size -= bytesRead;
        }

        output.close();
        System.out.println("Client uploaded file: " + fileName + " to dir: " + serverDestination + " successfully");
    }

    private void sendFile(String filePath) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        Set<String> files = new LinkedHashSet<String>(SomeTools.allSubDirectories(ServerProgram.getServerDir()));
        if (!files.contains(filePath)) {
            System.out.println("No requested file on server!");
            out.write("No file!");
            out.newLine();
            out.flush();
        } else {
            out.write("Sending requested file!");
            out.newLine();
            out.flush();

            File fileToSend = new File(filePath);
            byte[] byteArray = new byte[(int) fileToSend.length()];

            FileInputStream fis = new FileInputStream(fileToSend);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(byteArray, 0, byteArray.length);

            OutputStream os = clientSocket.getOutputStream();

            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(fileToSend.getName());
            dos.writeLong(byteArray.length);
            dos.write(byteArray, 0, byteArray.length);
            dos.flush();

            System.out.println("File sent!");
        }
    }

    private void listFiles() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        Set<String> files = new LinkedHashSet<String>(SomeTools.allSubDirectories(ServerProgram.getServerDir()));
        out.write("------------------------------------------------------------------------------------------");
        out.newLine();
        out.flush();
        for (String s : files) {
            out.write(s);
            out.newLine();
            out.flush();
        }
        out.write("------------------------------------------------------------------------------------------");
        out.newLine();
        out.flush();
        out.write("ENDFILES");
        out.newLine();
        out.flush();
    }

}
