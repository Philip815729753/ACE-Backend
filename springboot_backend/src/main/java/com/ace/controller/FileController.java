package com.ace.controller;

import com.ace.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.ByteArrayResource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;


    // Handle POST request for file upload
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            fileService.uploadFile(file);
            return "File uploaded and data stored successfully!";
        } catch (IOException e) {
            return "Error occurred: " + e.getMessage();
        }
    }
    //Handle GET request for team fixture number file export(for client)
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCompetitionData(
            @RequestParam("competitionId") int competitionId
            ) throws IOException {

        ByteArrayInputStream inputStream = fileService.exportCompetitionDataToExcel(competitionId);

        HttpHeaders headers = new HttpHeaders();
        String filename = URLEncoder.encode("competition_data.xlsx", StandardCharsets.UTF_8);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");


        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(inputStream.readAllBytes());
    }

    //Handle GET request for competition detail file export(for teacher)
    @GetMapping("/exportDetail")
    public ResponseEntity<ByteArrayResource> exportTeamsToExcel(@RequestParam("competitionId") int competitionId){
        try {
        byte[] excelData = fileService.generateDetailedExcelReport(competitionId);

        ByteArrayResource resource = new ByteArrayResource(excelData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teams_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
        } catch (IOException e) {
           return ResponseEntity.notFound().build();
        }
        }
}
