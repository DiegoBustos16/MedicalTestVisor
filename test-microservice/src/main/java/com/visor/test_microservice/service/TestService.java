package com.visor.test_microservice.service;

import com.visor.test_microservice.entity.TestEntity;
import com.visor.test_microservice.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;

    public TestEntity createTestEntity(TestEntity testEntity) {
        testEntity.setCreatedAt(Instant.now());
        return testRepository.save(testEntity);
    }

    public List<TestEntity> getAllTests() {
        return testRepository.findByDeletedAtIsNull();
    }

    public Optional<TestEntity> getTestById(String id) {
        return testRepository.findByIdAndDeletedAtIsNull(id);
    }

    public TestEntity updateTestEntity(String id, TestEntity testEntity) {
        if (testRepository.existsByIdAndDeletedAtIsNull(id)) {
            testEntity.setId(id);
            return testRepository.save(testEntity);
        }
        return null;
    }

    public void deleteTestEntity(String id) {
        Optional<TestEntity> testEntity = testRepository.findByIdAndDeletedAtIsNull(id);
        testEntity.ifPresent(entity -> {
            entity.setDeletedAt(Instant.now());
            testRepository.save(entity);
        });
    }
}