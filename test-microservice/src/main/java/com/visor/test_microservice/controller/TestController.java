package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.TestEntity;
import com.visor.test_microservice.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Autowired
    private TestService testService;

    @PostMapping
    public ResponseEntity<TestEntity> createTest(@RequestBody TestEntity testEntity) {
        TestEntity createdTest = testService.createTestEntity(testEntity);
        return new ResponseEntity<>(createdTest, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TestEntity>> getAllTests() {
        List<TestEntity> tests = testService.getAllTests();
        return new ResponseEntity<>(tests, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestEntity> getTestById(@PathVariable String id) {
        Optional<TestEntity> test = testService.getTestById(id);
        return test.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestEntity> updateTest(@PathVariable String id, @RequestBody TestEntity testEntity) {
        TestEntity updatedTest = testService.updateTestEntity(id, testEntity);
        return updatedTest != null ? new ResponseEntity<>(updatedTest, HttpStatus.OK)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable String id) {
        testService.deleteTestEntity(id);
        return ResponseEntity.noContent().build();
    }
}
