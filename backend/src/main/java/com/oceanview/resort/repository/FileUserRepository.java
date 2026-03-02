package com.oceanview.resort.repository;

import com.oceanview.resort.model.User;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileUserRepository implements UserRepository {
    private static final String DATA_DIR = "data";
    private static final String FILE_PATH = DATA_DIR + "/users.txt";

    public FileUserRepository() {
        initializeDataFile();
    }

    private void initializeDataFile() {
        try {
            Path dirPath = Paths.get(DATA_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = Paths.get(FILE_PATH);
            if (!Files.exists(filePath)) {
                Files.writeString(filePath, "admin,admin123,ADMIN\nstaff,staff123,STAFF\n");
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize users data: " + e.getMessage());
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path))
                return users;

            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    users.add(new User(parts[0], parts[1], parts[2]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users: " + e.getMessage());
        }
        return users;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public void save(User user) {
        List<User> users = findAll();
        users.removeIf(u -> u.getUsername().equals(user.getUsername()));
        users.add(user);
        saveAll(users);
    }

    @Override
    public void delete(String username) {
        List<User> users = findAll();
        users.removeIf(u -> u.getUsername().equals(username));
        saveAll(users);
    }

    private void saveAll(List<User> users) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (User user : users) {
                out.println(String.format("%s,%s,%s",
                        user.getUsername(), user.getPassword(), user.getRole()));
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}
