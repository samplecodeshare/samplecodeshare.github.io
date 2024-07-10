package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api")
public class ZipController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/execute")
    public ResponseEntity<String> uploadAndExecute(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("spring-boot-app");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Could not create temp directory");
        }

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(tempDir.toFile(), zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    Files.copy(zis, newFile.toPath());
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Could not extract the ZIP file");
        }

        File pomFile = new File(tempDir.toFile(), "pom.xml");
        if (!pomFile.exists()) {
            return ResponseEntity.badRequest().body("POM file is missing");
        }

        int port = findOpenPort();
        if (port == -1) {
            return ResponseEntity.status(500).body("Could not find an open port");
        }

        String result = applicationService.startApplication(tempDir.toFile(), port);
        JettyReverseProxy.addAlias("/" + port, port);

        return ResponseEntity.ok(result);
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private int findOpenPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
