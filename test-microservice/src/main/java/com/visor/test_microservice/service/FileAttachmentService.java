package com.visor.test_microservice.service;

import com.visor.test_microservice.entity.FileAttachment;
import com.visor.test_microservice.exception.InvalidFileException;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.FileAttachmentRepository;
import com.visor.test_microservice.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final TestRepository testRepository;
    private final S3Service s3Service;

    public FileAttachment saveFileAttachment(MultipartFile file, String testId) {
        if (!testRepository.existsByIdAndDeletedAtIsNull(testId)) {
            throw new ResourceNotFoundException("No active Test found with ID: " + testId);
        }

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("The file is empty or null");
        }

        FileAttachment fileAttachment = new FileAttachment();
        try {
            String url = s3Service.uploadFile(file);
            fileAttachment.setFileUrl(url);
            fileAttachment.setTestId(testId);
            fileAttachment.setFileName(file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
        }

        return fileAttachmentRepository.save(fileAttachment);
    }

    public List<FileAttachment> getAttachmentsByTestEntityId(String testEntityId) {
        if (!testRepository.existsByIdAndDeletedAtIsNull(testEntityId)) {
            throw new ResourceNotFoundException("No active Test found with ID: " + testEntityId);
        }

        return fileAttachmentRepository.findByTestIdAndDeletedAtIsNull(testEntityId);
    }

    public void deleteFileAttachment(String id) {
        FileAttachment fileAttachment = fileAttachmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("No active attachment found with ID: " + id));

        fileAttachment.setDeletedAt(Instant.now());
        fileAttachmentRepository.save(fileAttachment);
    }
}
