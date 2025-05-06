package com.visor.test_microservice.service;

import com.visor.test_microservice.client.HospitalClient;
import com.visor.test_microservice.client.PatientClient;
import com.visor.test_microservice.dto.FileAttachmentDTO;
import com.visor.test_microservice.dto.ImageFileDTO;
import com.visor.test_microservice.dto.ImageStackDTO;
import com.visor.test_microservice.dto.PatientTestDTO;
import com.visor.test_microservice.entity.FileAttachment;
import com.visor.test_microservice.entity.ImageFile;
import com.visor.test_microservice.entity.ImageStack;
import com.visor.test_microservice.entity.TestEntity;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.FileAttachmentRepository;
import com.visor.test_microservice.repository.ImageFileRepository;
import com.visor.test_microservice.repository.ImageStackRepository;
import com.visor.test_microservice.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TestService {

    private final TestRepository testRepository;
    private final ImageStackRepository imageStackRepository;
    private final ImageFileRepository imageFileRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final HospitalClient hospitalClient;
    private final PatientClient patientClient;


    public TestEntity createTestEntity(TestEntity testEntity) {

        if (!hospitalClient.existHospitalDoctorByDoctorIdAndHospitalId(testEntity.getDoctorId(), testEntity.getHospitalId())) {
            throw new ResourceNotFoundException("No active association found with Doctor Id: " + testEntity.getDoctorId() + " and Hospital Id: " + testEntity.getHospitalId());
        }

        if (patientClient.existPatientById(testEntity.getPatientId())) {
            throw new ResourceNotFoundException("No active patient found with Patient Id: " + testEntity.getPatientId());
        }

        testEntity.setPassCode(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10));
        return testRepository.save(testEntity);
    }

    public List<TestEntity> getAllTests() {
        return testRepository.findByDeletedAtIsNull();
    }

    public TestEntity getTestById(String id) {
        return testRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No active test found with ID: " + id));
    }

    public PatientTestDTO getPatientTestByPasscode(String passcode) {
        if (!passcode.matches("^[A-Za-z0-9]{10}$")) {
            throw new IllegalArgumentException("Invalid test passcode format");
        }

        TestEntity test = testRepository.findByPassCodeAndDeletedAtIsNull(passcode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No active test found with code: " + passcode));

        PatientTestDTO dto = new PatientTestDTO();
        dto.setId(test.getId());
        dto.setDoctorId(test.getDoctorId());
        dto.setPatientId(test.getPatientId());
        dto.setHospitalId(test.getHospitalId());
        dto.setCreatedAt(test.getCreatedAt());
        dto.setDeletedAt(test.getDeletedAt());
        dto.setPassCode(test.getPassCode());

        //Get imageStacks for the test
        List<ImageStack> stacks = imageStackRepository.findByTestIdAndDeletedAtIsNull(test.getId());
        List<ImageStackDTO> stackDTOs = new ArrayList<>();
        for (ImageStack stack : stacks) {
            ImageStackDTO stackDTO = new ImageStackDTO();
            stackDTO.setId(stack.getId());
            stackDTO.setStackName(stack.getStackName());
            stackDTO.setCreatedAt(stack.getCreatedAt());
            stackDTO.setDeletedAt(stack.getDeletedAt());
            stackDTO.setTestId(stack.getTestId());

            //Get the imageFiles for each stack
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

        //Get fileAttachments for the test
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
            if (!patientClient.existPatientById(updateTestEntity.getPatientId())) {
                throw new ResourceNotFoundException("No active patient found with Patient Id: " + updateTestEntity.getPatientId());
            }
            test.setPatientId(updateTestEntity.getPatientId());
        }

        if (updateTestEntity.getHospitalId() != null) {
            if (!hospitalClient.existHospitalDoctorByDoctorIdAndHospitalId(test.getDoctorId(), updateTestEntity.getHospitalId())) {
                throw new ResourceNotFoundException("No active association found with Doctor Id: " + test.getDoctorId() + " and Hospital Id: " + updateTestEntity.getHospitalId());
            }
            test.setHospitalId(updateTestEntity.getHospitalId());
        }

        return testRepository.save(test);
    }


    public void deleteTestEntity(String id) {
        TestEntity test = testRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cannot delete. No active test found with ID: " + id));

        test.setDeletedAt(Instant.now());
        testRepository.save(test);
    }
}
