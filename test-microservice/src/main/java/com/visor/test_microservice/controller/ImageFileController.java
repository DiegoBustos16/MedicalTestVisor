package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.ImageFile;
import com.visor.test_microservice.service.ImageFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/image-files")
public class ImageFileController {

    @Autowired
    private ImageFileService imageFileService;

    @PostMapping
    public ResponseEntity<ImageFile> saveImageFile(@RequestBody ImageFile imageFile) {
        ImageFile savedImage = imageFileService.saveImageFile(imageFile);
        return new ResponseEntity<>(savedImage, HttpStatus.CREATED);
    }

    @GetMapping("/stack/{imageStackId}")
    public ResponseEntity<List<ImageFile>> getImageFilesByImageStackId(@PathVariable Long imageStackId) {
        List<ImageFile> files = imageFileService.getImageFilesByImageStackId(imageStackId);
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImageFile(@PathVariable Long id) {
        imageFileService.deleteImageFile(id);
        return ResponseEntity.noContent().build();
    }
}
