package org.filesystem;

import java.util.*;

public class Find {
    public static String PROGRAM_NAME = "find";

    public static void main(String[] args) throws Exception {
        Kernel.initialize();
        String name = args[0];
        // stat the name to get information about the file or directory
        Stat stat = new Stat();
        //DFS---------------------------------------------------------------
        System.out.println("Find using DFS");
        findPathDFS(name,stat);
        //BFS----------------------------------------------------------------
        System.out.println("\nFind using BFS");
        findPathBFS(name, stat);
        //non-recursive(DFS)--------------------------------------------------------
        System.out.println("\nNon-recursive Find (DFS)");
        nonRecursiveFindPath(name, stat);
        // exit with success if we process all the arguments
        Kernel.exit(0);
    }

    private static void findPathBFS(String pathName, Stat stat) throws Exception{
        printPath(pathName,stat);
        short type = getFileType(stat);
        if(type==Kernel.S_IFDIR)
        BFS(pathName,stat);
    }


    private static void findPathDFS(String pathName, Stat stat) throws Exception {
        printPath(pathName, stat);
        short type = getFileType(stat);
        if (type == Kernel.S_IFDIR) {
            int fd = openDirectory(pathName);
            DirectoryEntry directoryEntry = new DirectoryEntry();
            Integer status = 0;
            while (nextDirectoryExists(status, fd, directoryEntry)) {
                String entryName = directoryEntry.getName();
                if (correct(entryName)) {
                    String nextPath = setNextPath(pathName, entryName);
                    checkStatus(nextPath, stat);
                    findPathDFS(nextPath, stat);
                }
            }
           closeDirectory(status, fd);
        }
    }

    private static void closeDirectory(Integer status, int fd) throws Exception {
        checkLastStatus(status);
        Kernel.close(fd);
    }

    private static short getFileType(Stat stat){
        return (short) (stat.getMode() & Kernel.S_IFMT);
    }

    private static int openDirectory(String pathName) throws Exception {
        int fd = Kernel.open(pathName, Kernel.O_RDONLY);
        checkFD(fd, pathName);
        return fd;
    }

    private static boolean nextDirectoryExists(Integer status, int fd, DirectoryEntry directoryEntry) throws Exception {
        status = Kernel.readdir(fd, directoryEntry);
        return status > 0;
    }

    private static void nonRecursiveFindPath(String pathName, Stat stat) throws Exception {
        short type = 0;
        Stack<String> stack = new Stack<>();
        stack.push(pathName);
        while (!stack.isEmpty()) {
            String path = stack.pop();
            printPath(path, stat);
            type = getFileType(stat);
            if (type == Kernel.S_IFDIR) {
                int fd = openDirectory(path);
                DirectoryEntry directoryEntry = new DirectoryEntry();
                Integer status = 0;
                while (nextDirectoryExists(status,fd,directoryEntry)) {
                    String entryName = directoryEntry.getName();
                    if (correct(entryName)) {
                        String nextPath = setNextPath(path, entryName);
                        pushNextPath(stack,nextPath,stat);
                    }
                }
                closeDirectory(status,fd);
            }
        }
    }

    private static boolean correct(String entryName){
        return !entryName.equals(".") && !entryName.equals("..");
    }

    private static void pushNextPath(Stack<String>stack, String nextPath, Stat stat)
            throws Exception {
        checkStatus(nextPath, stat);
        stack.push(nextPath);
    }

    private static void BFS(String pathName, Stat stat) throws Exception {
        List<String> paths = new ArrayList<>();
        int fd = openDirectory(pathName);
        DirectoryEntry directoryEntry = new DirectoryEntry();
        Integer status = 0;
        while (nextDirectoryExists(status,fd,directoryEntry)) {
            String entryName = directoryEntry.getName();
            if (correct(entryName)) {
                String nextPath = setNextPath(pathName, entryName);
                printPath(nextPath, stat);
                addPathToListIfDir(paths,nextPath,stat);
            }
        }
        closeDirectory(status,fd);
        for (String path : paths)
            BFS(path, stat);
    }

    private static void addPathToListIfDir(List<String>paths,String nextPath, Stat stat){
        short type = getFileType(stat);
        if (type == Kernel.S_IFDIR)
            paths.add(nextPath);
    }


    private static void printPath(String pathName, Stat stat) throws Exception {
        checkStatus(pathName, stat);
        System.out.println(pathName);
    }

    private static void checkStatus(String pathName, Stat stat) throws Exception {
        int status = Kernel.stat(pathName, stat);
        if (status < 0) {
            Kernel.perror(PROGRAM_NAME);
            Kernel.exit(1);
        }
    }

    private static void checkFD(int fd, String pathName) throws Exception {
        if (fd < 0) {
            Kernel.perror(PROGRAM_NAME);
            System.err.println(PROGRAM_NAME +
                    ": unable to open \"" + pathName + "\" for reading");
            Kernel.exit(1);
        }
    }

    private static void checkLastStatus(int status) throws Exception {
        if (status < 0) {
            Kernel.perror("main");
            System.err.println("main: unable to read directory entry from /");
            Kernel.exit(2);
        }
    }

    private static String setNextPath(String pathName, String entryName) {
        if (pathName.equals("/"))
            return (pathName + entryName);
        else
            return (pathName + "/" + entryName);
    }
}
