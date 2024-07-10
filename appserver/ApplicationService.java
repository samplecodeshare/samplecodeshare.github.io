package com.example.demo;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class ApplicationService {

    private final Map<String, Process> runningApplications = new HashMap<>();
    private final Map<String, Future<?>> runningTasks = new HashMap<>();

    public String startApplication(File workingDir, int port) {
        String command = "mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=" + port;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(workingDir);
        processBuilder.command("sh", "-c", command);

        try {
            Process process = processBuilder.start();
            runningApplications.put(String.valueOf(port), process);
            Future<?> task = Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            runningTasks.put(String.valueOf(port), task);
            return "Spring Boot application started on port " + port;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to start Spring Boot application on port " + port;
        }
    }

    public String stopApplication(int port) {
        Process process = runningApplications.get(String.valueOf(port));
        if (process != null) {
            process.destroy();
            runningApplications.remove(String.valueOf(port));
            runningTasks.remove(String.valueOf(port));
            return "Spring Boot application stopped on port " + port;
        } else {
            return "No running application found on port " + port;
        }
    }

    public Map<String, Process> getRunningApplications() {
        return runningApplications;
    }
}
