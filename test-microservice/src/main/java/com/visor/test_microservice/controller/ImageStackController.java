package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.ImageStack;
import com.visor.test_microservice.service.ImageStackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/image-stacks")
public class ImageStackController {

    @Autowired
    private ImageStackService imageStackService;

    @PostMapping
    public ResponseEntity<ImageStack> createImageStack(@RequestBody ImageStack imageStack) {
        ImageStack createdStack = imageStackService.createImageStack(imageStack);
        return new ResponseEntity<>(createdStack, HttpStatus.CREATED);
    }

    @GetMapping("/test/{testEntityId}")
    public ResponseEntity<List<ImageStack>> getImageStacksByTestEntityId(@PathVariable String testEntityId) {
        List<ImageStack> stacks = imageStackService.getImageStacksByTestEntityId(testEntityId);
        return new ResponseEntity<>(stacks, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImageStack(@PathVariable Long id) {
        imageStackService.deleteImageStack(id);
        return ResponseEntity.noContent().build();
    }
}
