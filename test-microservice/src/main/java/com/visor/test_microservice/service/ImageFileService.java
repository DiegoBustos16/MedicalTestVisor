package com.visor.test_microservice.service;

import com.visor.test_microservice.entity.ImageFile;
import com.visor.test_microservice.exception.InvalidFileException;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.ImageFileRepository;
import com.visor.test_microservice.repository.ImageStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageFileService {

    private final ImageFileRepository imageFileRepository;
    private final ImageStackRepository imageStackRepository;
    private final S3Service s3Service;

    public ImageFile saveImageFile(MultipartFile file, String imageStackId) {

        if (!imageStackRepository.existsById(imageStackId)) {
            throw new ResourceNotFoundException("No active Image Stack found with ID: " + imageStackId);
        }

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("The file is empty or null");
        }

        ImageFile imageFile = new ImageFile();
        try {
            String url = s3Service.uploadFile(file);
            imageFile.setFileUrl(url);
            imageFile.setImageStackId(imageStackId);
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
        }

        return imageFileRepository.save(imageFile);
    }

    public List<ImageFile> getImageFilesByImageStackId(String imageStackId) {
        if (!imageStackRepository.existsById(imageStackId)) {
            throw new ResourceNotFoundException("No active Image Stack found with ID: " + imageStackId);
        }

        return imageFileRepository.findByImageStackIdAndDeletedAtIsNull(imageStackId);
    }

    public void deleteImageFile(String id) {
        ImageFile imageFile = imageFileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("No active image file found with ID: " + id));

        imageFile.setDeletedAt(Instant.now());
        imageFileRepository.save(imageFile);
    }
}
