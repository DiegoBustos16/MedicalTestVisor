package com.visor.test_microservice.service;

import com.visor.test_microservice.dto.UpdateTestDTO;
import com.visor.test_microservice.entity.TestEntity;
import com.visor.test_microservice.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;

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

    public Optional<TestEntity> getTestByPasscode(String passcode) {
        return testRepository.findByPassCodeAndDeletedAtIsNull(passcode);
    }

    public TestEntity updateTestEntity(String id, UpdateTestDTO updateTestDTO) {
        TestEntity test = testRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("TestEntity not found"));

        test.setAttachments(updateTestDTO.getAttachments());
        test.setImageStacks(updateTestDTO.getImageStacks());

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