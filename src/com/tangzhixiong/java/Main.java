package com.tangzhixiong.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayDeque;

public class Main {
    public static WatchService watchService;
    public static Runtime runtime;

    public static void register(String srcDir) {
        try {
            if (watchService != null) {
                watchService.close();
            }
            watchService = FileSystems.getDefault().newWatchService();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (watchService == null) { return; }
        final ArrayDeque<File> queue = new ArrayDeque<>();
        queue.add(new File(srcDir));
        while (!queue.isEmpty()) {
            File pwd = queue.poll(); // it's a dir
            try {
                // watch this dir
                WatchKey key = pwd.toPath().register(watchService
                        , StandardWatchEventKinds.ENTRY_MODIFY
                        , StandardWatchEventKinds.ENTRY_CREATE );
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            final File[] entries;
            try {
                entries = pwd.listFiles();
            }
            catch (NullPointerException e) { continue; }
            for (File entry: entries) {
                if (entry.isDirectory()) {
                    final String basename = entry.getName();
                    if (!basename.startsWith(".") && !basename.equals("publish")) {
                        queue.add(entry);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        String srcDir = ".";
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-d") || args[i].equals("-directory")) {
                if (i+1 < args.length) {
                    srcDir = args[++i];
                }
            }
        }

        runtime = Runtime.getRuntime();
        register(srcDir);
        System.out.println("[ ] watching...");
        while (true) {
            WatchKey key = null;
            try {
                key = watchService.take();
                if (key == null) {
                    throw new InterruptedException();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            System.out.println("[ ] watching...");
            if (!key.pollEvents().isEmpty()) {
                try {
                    runtime.exec("make");
                    System.out.println("[*] making...");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!key.reset()) {
                break;
            }
        }
    }
}
