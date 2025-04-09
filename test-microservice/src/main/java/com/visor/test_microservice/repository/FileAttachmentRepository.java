package com.visor.test_microservice.repository;

import com.visor.test_microservice.entity.FileAttachment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FileAttachmentRepository extends MongoRepository<FileAttachment, String> {
    List<FileAttachment> findByTestIdAndDeletedAtIsNull(String testEntityId);

    Optional<FileAttachment> findByIdAndDeletedAtIsNull(String id);
}
