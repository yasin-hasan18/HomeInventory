package com.mycompany.home;

import java.io.*;
import java.util.*;

public class FileHelper {

    private static final String DELIM = ":::";
    public static final String USERS_FILE = "users.txt";
    public static final String HOMES_FILE = "homes.txt";
    public static final String ROOMS_FILE = "rooms.txt";
    public static final String INVENTORY_FILE = "inventory.txt";

    public static void initializeFiles() {
        createFileIfNotExists(USERS_FILE);
        createFileIfNotExists(HOMES_FILE);
        createFileIfNotExists(ROOMS_FILE);
        createFileIfNotExists(INVENTORY_FILE);
        System.out.println("Text file storage ready.");
    }

    private static void createFileIfNotExists(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> readLines(String fileName) {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                data.add(line.split(DELIM, -1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void writeLines(String fileName, List<String[]> data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (String[] row : data) {
                bw.write(String.join(DELIM, row));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendLine(String fileName, String[] row) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            bw.write(String.join(DELIM, row));
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getNextId(String fileName) {
        List<String[]> data = readLines(fileName);
        int maxId = 0;
        for (String[] row : data) {
            if (row.length > 0) {
                try {
                    int id = Integer.parseInt(row[0]);
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException e) {
                   
                }
            }
        }
        return maxId + 1;
    }
}
