package com.visor.test_microservice.service;

import com.visor.test_microservice.entity.ImageFile;
import com.visor.test_microservice.repository.ImageFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ImageFileService {

    @Autowired
    private ImageFileRepository imageFileRepository;

    public ImageFile saveImageFile(ImageFile imageFile) {
        return imageFileRepository.save(imageFile);
    }

    public List<ImageFile> getImageFilesByImageStackId(String imageStackId) {
        return imageFileRepository.findByImageStackIdAndDeletedAtIsNull(imageStackId);
    }

    public void deleteImageFile(String id) {
        Optional<ImageFile> imageFile = imageFileRepository.findByIdAndDeletedAtIsNull(id);
        imageFile.ifPresent(file -> {
            file.setDeletedAt(Instant.now());
            imageFileRepository.save(file);
        });
    }
}
