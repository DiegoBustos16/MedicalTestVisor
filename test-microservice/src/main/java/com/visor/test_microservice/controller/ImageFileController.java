package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.ImageFile;
import com.visor.test_microservice.service.ImageFileService;
import com.visor.test_microservice.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tests/image-files")
public class ImageFileController {

    @Autowired
    private ImageFileService imageFileService;

    @Autowired
    private S3Service s3Service;

    @PostMapping
    public ResponseEntity<?> saveImageFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam("imageStackId") String imageStackId) {
        ImageFile imageFile = new ImageFile();
        try {
            String url = s3Service.uploadFile(file);
            imageFile.setFileUrl(url);
            imageFile.setImageStackId(imageStackId);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file");
        }
        ImageFile savedImage = imageFileService.saveImageFile(imageFile);
        return new ResponseEntity<>(savedImage, HttpStatus.CREATED);
    }

    @GetMapping("/stack/{imageStackId}")
    public ResponseEntity<List<ImageFile>> getImageFilesByImageStackId(@PathVariable String imageStackId) {
        List<ImageFile> files = imageFileService.getImageFilesByImageStackId(imageStackId);
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImageFile(@PathVariable String id) {
        imageFileService.deleteImageFile(id);
        return ResponseEntity.noContent().build();
    }
}
