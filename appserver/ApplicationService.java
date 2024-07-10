package com.example.demo;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ApplicationService {

    private final ConcurrentMap<Integer, Process> runningApplications = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Future<?>> runningTasks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> aliasToPortMap = new ConcurrentHashMap<>();
    private final String stateFilePath = "application_state.yaml";
    private final Lock fileWriteLock = new ReentrantLock();

    public ApplicationService() {
        loadStateFromFile();
    }

    public String startApplication(File workingDir, int port) {
        String command = "mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=" + port;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(workingDir);
        processBuilder.command("sh", "-c", command);

        try {
            Process process = processBuilder.start();
            runningApplications.put(port, process);
            Future<?> task = Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            runningTasks.put(port, task);
            saveStateToFile();
            return "Spring Boot application started on port " + port;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to start Spring Boot application on port " + port;
        }
    }

    public String stopApplication(int port) {
        Process process = runningApplications.get(port);
        if (process != null) {
            process.destroy();
            runningApplications.remove(port);
            runningTasks.remove(port);
            saveStateToFile();
            return "Spring Boot application stopped on port " + port;
        } else {
            return "No running application found on port " + port;
        }
    }

    public Map<Integer, Process> getRunningApplications() {
        return new HashMap<>(runningApplications);
    }

    private void loadStateFromFile() {
        try (FileReader fileReader = new FileReader(stateFilePath)) {
            Yaml yaml = new Yaml();
            Map<String, Object> state = yaml.load(fileReader);

            if (state != null) {
                Map<Integer, Integer> portAliasMap = (Map<Integer, Integer>) state.get("portAliasMap");
                if (portAliasMap != null) {
                    for (Map.Entry<Integer, Integer> entry : portAliasMap.entrySet()) {
                        aliasToPortMap.put(String.valueOf(entry.getValue()), entry.getKey());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load application state from file: " + e.getMessage());
        }
    }

    private void saveStateToFile() {
        fileWriteLock.lock();
        try (FileWriter fileWriter = new FileWriter(stateFilePath)) {
            Map<String, Object> state = new HashMap<>();
            Map<Integer, Integer> portAliasMap = new HashMap<>();

            for (Map.Entry<String, Integer> entry : aliasToPortMap.entrySet()) {
                portAliasMap.put(entry.getValue(), Integer.parseInt(entry.getKey()));
            }

            state.put("portAliasMap", portAliasMap);

            Yaml yaml = new Yaml();
            yaml.dump(state, fileWriter);
        } catch (IOException e) {
            System.err.println("Failed to save application state to file: " + e.getMessage());
        } finally {
            fileWriteLock.unlock();
        }
    }
}
