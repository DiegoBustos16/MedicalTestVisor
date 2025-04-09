package com.visor.test_microservice.repository;

import com.visor.test_microservice.entity.TestEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TestRepository extends MongoRepository<TestEntity, String> {
    List<TestEntity> findByDeletedAtIsNull();

    Optional<TestEntity> findByIdAndDeletedAtIsNull(String id);

    Optional<TestEntity> findByPassCodeAndDeletedAtIsNull(String passcode);

    boolean existsByIdAndDeletedAtIsNull(String id);
}
