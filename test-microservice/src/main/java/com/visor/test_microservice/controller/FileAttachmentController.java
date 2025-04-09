package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.FileAttachment;
import com.visor.test_microservice.service.FileAttachmentService;
import com.visor.test_microservice.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tests/file-attachments")
public class FileAttachmentController {

    @Autowired
    private FileAttachmentService fileAttachmentService;

    @Autowired
    private S3Service s3Service;

    @PostMapping
    public ResponseEntity<?> saveFileAttachment(@RequestParam("file") MultipartFile file,
                                                               @RequestParam("testId") String testId) {
        FileAttachment fileAttachment = new FileAttachment();
        try {
            String url = s3Service.uploadFile(file);
            fileAttachment.setFileUrl(url);
            fileAttachment.setTestId(testId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file");
        }
        FileAttachment savedFile = fileAttachmentService.saveFileAttachment(fileAttachment);
        return new ResponseEntity<>(savedFile, HttpStatus.CREATED);
    }

    @GetMapping("/test/{testEntityId}")
    public ResponseEntity<List<FileAttachment>> getAttachmentsByTestEntityId(@PathVariable String testEntityId) {
        List<FileAttachment> attachments = fileAttachmentService.getAttachmentsByTestEntityId(testEntityId);
        return new ResponseEntity<>(attachments, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFileAttachment(@PathVariable String id) {
        fileAttachmentService.deleteFileAttachment(id);
        return ResponseEntity.noContent().build();
    }
}