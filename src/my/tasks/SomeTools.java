package my.tasks;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SomeTools {

    public static String[] parseArgs(String inputString) {
        String localStr = inputString.trim();
        ArrayList<String> args = new ArrayList<String>();
        StringBuilder currentStr = new StringBuilder();

        boolean meetDQ = false;
        boolean meetQ = false;

        for (int i = 0; i < localStr.length(); i++) {
            char cChar = localStr.charAt(i);
            int cLen = currentStr.length();

            if (cChar == '\"') {
                if (meetDQ) {
                    if (cLen != 0) {
                        args.add(currentStr.toString().trim());
                        currentStr = new StringBuilder();
                    }
                    meetDQ = false;
                } else {
                    meetDQ = true;
                }
            } else if (cChar == '\'') {
                if (meetQ) {
                    if (cLen != 0) {
                        args.add(currentStr.toString().trim());
                        currentStr = new StringBuilder();
                    }
                    meetQ = false;
                } else {
                    meetQ = true;
                }
            } else if (cChar == ' ' && !meetDQ && !meetQ && cLen != 0) {
                args.add(currentStr.toString().trim());
                currentStr = new StringBuilder();
            } else currentStr.append(cChar);
        }
        if (currentStr.length() != 0) {
            args.add(currentStr.toString());
        }

        return args.toArray(new String[args.size()]);
    }

    /**
     * Not recursively traverses files and directories in given directory.
     * Returns a list of paths to traversed files.
     *
     * @param initialDirectory File object which represents main directory.
     * @return List of files paths
     */
    public static List<String> allSubDirectories(File initialDirectory) {
        List<String> listOfFiles = new ArrayList<String>(); //Empty list which will be filled with files

        Queue<String> queueOfFiles = new ArrayDeque<String>(); //Queue which will be used for traverse
        queueOfFiles.add(initialDirectory.getPath());   //Adding initialDirectory to queue

        while (!queueOfFiles.isEmpty()) {
            File currentDir = new File(queueOfFiles.remove()); //Extracting directory from queue to traverse its content

            try {
                File[] files = currentDir.listFiles();  //getting directories content
                if (files == null)
                    continue;    //Some directories and files are hidden or system and will throw NullPtr exception
                for (File file : files) {
                    if (file.isDirectory()) {   //if one is directory then add it to queue to process later
                        queueOfFiles.add(file.getPath());
                    } else listOfFiles.add(file.getPath()); //else it is file so add it to list
                }
            } catch (SecurityException e) {
                System.err.println("Access denied: " + e.getMessage());
            }
        }

        return listOfFiles;
    }
}
