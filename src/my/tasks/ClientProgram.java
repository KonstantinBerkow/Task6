package my.tasks;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ClientProgram {

    private static Socket socket;
    private static boolean working = true;

    public static void main(String[] args) throws IOException {
        InetAddress ip = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
        socket = new Socket(ip, port);

        BufferedReader localInput = new BufferedReader(new InputStreamReader(System.in));

        while (working) {
            String inputLine = localInput.readLine();
            if (inputLine.equals("disconnect")) {
                disconnect(socket);
            } else {
                String[] commandArgs = SomeTools.parseArgs(inputLine);
                if (commandArgs[0].equals("files")) {
                    listFiles();
                } else if (commandArgs[0].equals("download")) {
                    download(commandArgs[1], commandArgs[2]);
                } else if (commandArgs[0].equals("upload")) {
                    upload(commandArgs[1], commandArgs[2]);
                }
            }
        }

        socket.close();
        localInput.close();
    }

    private static void upload(String pathToFile, String serverDestination) throws IOException {
        File fileToTransfer = new File(pathToFile);
        if (fileToTransfer.exists()) {
            System.out.println("Sending file to server!");
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.write(String.format("upload '%s'", serverDestination));
            out.newLine();
            out.flush();
            byte[] byteArray = new byte[(int) fileToTransfer.length()];

            FileInputStream fis = new FileInputStream(fileToTransfer);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(byteArray, 0, byteArray.length);

            OutputStream os = socket.getOutputStream();

            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(fileToTransfer.getName());
            dos.writeLong(byteArray.length);
            dos.write(byteArray, 0, byteArray.length);
            dos.flush();
            System.out.println("File received by server!");
        } else System.out.println("File does not exist!");
    }

    private static void download(String serverFilePath, String clientDestination) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        out.write(String.format("download '%s' '%s'", serverFilePath, clientDestination));
        out.newLine();
        out.flush();

        String answer = in.readLine();
        if (answer.equals("No file!")) {
            System.out.println("No such file on server!");
        } else if (answer.equals("Sending requested file!")) {
            System.out.println("File download begun!");

            int bytesRead;
            DataInputStream clientData = new DataInputStream(socket.getInputStream());

            String fileName = clientData.readUTF();

            OutputStream output;
            if (clientDestination.lastIndexOf(File.separatorChar) != clientDestination.length() - 1)
                output = new FileOutputStream((clientDestination + File.separator + fileName));
            else output = new FileOutputStream((clientDestination + fileName));

            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            output.close();

            System.out.println("File successfully downloaded!");
        }
    }

    private static void listFiles() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        out.write("files");
        out.newLine();
        out.flush();

        String tmpInput = in.readLine();
        while (!tmpInput.equals("ENDFILES")) {
            System.out.println(tmpInput);
            tmpInput = in.readLine();
        }
    }

    private static void disconnect(Socket socket) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        out.write("disconnect");
        out.newLine();
        out.flush();
        working = false;
    }
}

