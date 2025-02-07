package com.visor.test_microservice.service;

import com.visor.test_microservice.entity.FileAttachment;
import com.visor.test_microservice.repository.FileAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class FileAttachmentService {

    @Autowired
    private FileAttachmentRepository fileAttachmentRepository;

    public FileAttachment saveFileAttachment(FileAttachment fileAttachment) {
        fileAttachment.setCreatedAt(Instant.now());
        return fileAttachmentRepository.save(fileAttachment);
    }

    public List<FileAttachment> getAttachmentsByTestEntityId(String testEntityId) {
        return fileAttachmentRepository.findByTestEntityIdAndDeletedAtIsNull(testEntityId);
    }

    public void deleteFileAttachment(Long id) {
        Optional<FileAttachment> fileAttachment = fileAttachmentRepository.findByIdAndDeletedAtIsNull(id);
        fileAttachment.ifPresent(file -> {
            file.setDeletedAt(Instant.now());
            fileAttachmentRepository.save(file);
        });
    }
}
