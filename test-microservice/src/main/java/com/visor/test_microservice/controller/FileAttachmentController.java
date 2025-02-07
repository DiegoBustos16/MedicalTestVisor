package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.FileAttachment;
import com.visor.test_microservice.service.FileAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file-attachments")
public class FileAttachmentController {

    @Autowired
    private FileAttachmentService fileAttachmentService;

    @PostMapping
    public ResponseEntity<FileAttachment> saveFileAttachment(@RequestBody FileAttachment fileAttachment) {
        FileAttachment savedFile = fileAttachmentService.saveFileAttachment(fileAttachment);
        return new ResponseEntity<>(savedFile, HttpStatus.CREATED);
    }

    @GetMapping("/test/{testEntityId}")
    public ResponseEntity<List<FileAttachment>> getAttachmentsByTestEntityId(@PathVariable String testEntityId) {
        List<FileAttachment> attachments = fileAttachmentService.getAttachmentsByTestEntityId(testEntityId);
        return new ResponseEntity<>(attachments, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFileAttachment(@PathVariable Long id) {
        fileAttachmentService.deleteFileAttachment(id);
        return ResponseEntity.noContent().build();
    }
}