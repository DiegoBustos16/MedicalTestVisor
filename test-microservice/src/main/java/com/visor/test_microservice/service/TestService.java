package com.visor.test_microservice.service;

import com.visor.test_microservice.dto.FileAttachmentDTO;
import com.visor.test_microservice.dto.ImageFileDTO;
import com.visor.test_microservice.dto.ImageStackDTO;
import com.visor.test_microservice.dto.PatientTestDTO;
import com.visor.test_microservice.entity.FileAttachment;
import com.visor.test_microservice.entity.ImageFile;
import com.visor.test_microservice.entity.ImageStack;
import com.visor.test_microservice.entity.TestEntity;
import com.visor.test_microservice.repository.FileAttachmentRepository;
import com.visor.test_microservice.repository.ImageFileRepository;
import com.visor.test_microservice.repository.ImageStackRepository;
import com.visor.test_microservice.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;
    @Autowired
    private final ImageStackRepository imageStackRepository;
    @Autowired
    private final ImageFileRepository imageFileRepository;
    @Autowired
    private final FileAttachmentRepository fileAttachmentRepository;

    public TestService(ImageStackRepository imageStackRepository, ImageFileRepository imageFileRepository, FileAttachmentRepository fileAttachmentRepository) {
        this.imageStackRepository = imageStackRepository;
        this.imageFileRepository = imageFileRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
    }

    public TestEntity createTestEntity(TestEntity testEntity) {
        testEntity.setPassCode(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10));
        return testRepository.save(testEntity);
    }

    public List<TestEntity> getAllTests() {
        return testRepository.findByDeletedAtIsNull();
    }

    public Optional<TestEntity> getTestById(String id) {
        return testRepository.findByIdAndDeletedAtIsNull(id);
    }

    public PatientTestDTO getPatientTestByPasscode(String passcode) {
        Optional<TestEntity> optionalTest = testRepository.findByPassCodeAndDeletedAtIsNull(passcode);
        if (optionalTest.isEmpty()) {
            return null;
        }
        TestEntity test = optionalTest.get();
        PatientTestDTO dto = new PatientTestDTO();
        dto.setId(test.getId());
        dto.setDoctorId(test.getDoctorId());
        dto.setPatientId(test.getPatientId());
        dto.setHospitalId(test.getHospitalId());
        dto.setCreatedAt(test.getCreatedAt());
        dto.setDeletedAt(test.getDeletedAt());
        dto.setPassCode(test.getPassCode());

        // Obtener imageStacks para el test
        List<ImageStack> stacks = imageStackRepository.findByTestIdAndDeletedAtIsNull(test.getId());
        List<ImageStackDTO> stackDTOs = new ArrayList<>();
        for (ImageStack stack : stacks) {
            ImageStackDTO stackDTO = new ImageStackDTO();
            stackDTO.setId(stack.getId());
            stackDTO.setStackName(stack.getStackName());
            stackDTO.setCreatedAt(stack.getCreatedAt());
            stackDTO.setDeletedAt(stack.getDeletedAt());
            stackDTO.setTestId(stack.getTestId());

            // Obtener los imageFiles para cada stack
            List<ImageFile> imageFiles = imageFileRepository.findByImageStackIdAndDeletedAtIsNull(stack.getId());
            List<ImageFileDTO> imageFileDTOs = new ArrayList<>();
            for (ImageFile file : imageFiles) {
                ImageFileDTO fileDTO = new ImageFileDTO();
                fileDTO.setId(file.getId());
                fileDTO.setFileUrl(file.getFileUrl());
                fileDTO.setCreatedAt(file.getCreatedAt());
                fileDTO.setDeletedAt(file.getDeletedAt());
                fileDTO.setImageStackId(file.getImageStackId());
                imageFileDTOs.add(fileDTO);
            }
            stackDTO.setImageFiles(imageFileDTOs);
            stackDTOs.add(stackDTO);
        }
        dto.setImageStacks(stackDTOs);

        // Obtener fileAttachments para el test
        List<FileAttachment> attachments = fileAttachmentRepository.findByTestIdAndDeletedAtIsNull(test.getId());
        List<FileAttachmentDTO> attachmentDTOs = new ArrayList<>();
        for (FileAttachment att : attachments) {
            FileAttachmentDTO attDTO = new FileAttachmentDTO();
            attDTO.setId(att.getId());
            attDTO.setFileName(att.getFileName());
            attDTO.setFileUrl(att.getFileUrl());
            attDTO.setCreatedAt(att.getCreatedAt());
            attDTO.setDeletedAt(att.getDeletedAt());
            attDTO.setTestId(att.getTestId());
            attachmentDTOs.add(attDTO);
        }
        dto.setFileAttachments(attachmentDTOs);

        return dto;
    }


    public TestEntity updateTestEntity(String id, TestEntity updateTestEntity) {
        TestEntity test = testRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("TestEntity not found"));

        if (updateTestEntity.getPatientId() != null) {
            test.setPatientId(updateTestEntity.getPatientId());
        }

        if (updateTestEntity.getHospitalId() != null) {
            test.setHospitalId(updateTestEntity.getHospitalId());
        }

        return testRepository.save(test);
    }



    public void deleteTestEntity(String id) {
        Optional<TestEntity> testEntity = testRepository.findByIdAndDeletedAtIsNull(id);
        testEntity.ifPresent(entity -> {
            entity.setDeletedAt(Instant.now());
            testRepository.save(entity);
        });
    }
}
