package com.visor.test_microservice.repository;

import com.visor.test_microservice.entity.ImageFile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ImageFileRepository extends MongoRepository<ImageFile, String> {
    List<ImageFile> findByImageStackIdAndDeletedAtIsNull(String imageStackId);

    Optional<ImageFile> findByIdAndDeletedAtIsNull(String id);
}
