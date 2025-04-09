package com.visor.test_microservice.repository;

import com.visor.test_microservice.entity.ImageStack;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ImageStackRepository extends MongoRepository<ImageStack, String> {
    List<ImageStack> findByTestIdAndDeletedAtIsNull(String testEntityId);

    Optional<ImageStack> findByIdAndDeletedAtIsNull(String id);
}
