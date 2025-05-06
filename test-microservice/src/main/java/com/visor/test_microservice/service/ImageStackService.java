package com.visor.test_microservice.service;

import com.visor.test_microservice.entity.ImageStack;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.ImageStackRepository;
import com.visor.test_microservice.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageStackService {

    private final ImageStackRepository imageStackRepository;
    private final TestRepository testRepository;

    public ImageStack createImageStack(ImageStack imageStack) {
        if (!testRepository.existsByIdAndDeletedAtIsNull(imageStack.getTestId())) {
            throw new ResourceNotFoundException("No active test found with ID: " + imageStack.getTestId());
        }
        return imageStackRepository.save(imageStack);
    }

    public List<ImageStack> getImageStacksByTestEntityId(String testEntityId) {
        if (!testRepository.existsByIdAndDeletedAtIsNull(testEntityId)) {
            throw new ResourceNotFoundException("No active test found with ID: " + testEntityId);
        }
        return imageStackRepository.findByTestIdAndDeletedAtIsNull(testEntityId);
    }

    public void deleteImageStack(String id) {
        ImageStack imageStack = imageStackRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("No active image stack found with ID: " + id));

        imageStack.setDeletedAt(Instant.now());
        imageStackRepository.save(imageStack);
    }
}
