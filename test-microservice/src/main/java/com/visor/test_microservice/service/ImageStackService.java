package com.visor.test_microservice.service;

import com.visor.test_microservice.entity.ImageStack;
import com.visor.test_microservice.repository.ImageStackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ImageStackService {

    @Autowired
    private ImageStackRepository imageStackRepository;

    public ImageStack createImageStack(ImageStack imageStack) {
        return imageStackRepository.save(imageStack);
    }

    public List<ImageStack> getImageStacksByTestEntityId(String testEntityId) {
        return imageStackRepository.findByTestEntityIdAndDeletedAtIsNull(testEntityId);
    }

    public void deleteImageStack(Long id) {
        Optional<ImageStack> imageStack = imageStackRepository.findByIdAndDeletedAtIsNull(id);
        imageStack.ifPresent(stack -> {
            stack.setDeletedAt(Instant.now());  // Seteamos la fecha de eliminación
            imageStackRepository.save(stack);
        });
    }
}
