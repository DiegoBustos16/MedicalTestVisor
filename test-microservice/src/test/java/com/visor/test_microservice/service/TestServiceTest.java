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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class TestServiceTest {

    @Mock
    private TestRepository testRepository;
    @Mock
    private ImageStackRepository imageStackRepository;
    @Mock
    private ImageFileRepository imageFileRepository;
    @Mock
    private FileAttachmentRepository fileAttachmentRepository;
    @Mock
    private HospitalClient hospitalClient;
    @Mock
    private PatientClient patientClient;

    @InjectMocks
    private TestService testService;

    @Nested
    @DisplayName("Create TestEntity")
    class CreateTestEntityTests {

        @Test
        @DisplayName("should save TestEntity when association and patient valid")
        void createTestEntity_shouldSave_whenValid() {
            TestEntity input = new TestEntity();
            input.setDoctorId(1L);
            input.setHospitalId(10L);
            input.setPatientId(100L);

            given(hospitalClient.existHospitalDoctorByDoctorIdAndHospitalId(1L, 10L)).willReturn(true);
            given(patientClient.existPatientById(100L)).willReturn(false);

            TestEntity saved = new TestEntity();
            saved.setId("abc123");
            saved.setDoctorId(1L);
            saved.setHospitalId(10L);
            saved.setPatientId(100L);
            saved.setPassCode("PASSCODE01");

            given(testRepository.save(any(TestEntity.class))).willReturn(saved);

            TestEntity result = testService.createTestEntity(input);

            assertThat(result).isNotNull();
            assertThat(result.getDoctorId()).isEqualTo(1L);
            assertThat(result.getHospitalId()).isEqualTo(10L);
            assertThat(result.getPatientId()).isEqualTo(100L);
            assertThat(result.getPassCode()).hasSize(10);
            verify(testRepository).save(any(TestEntity.class));
        }

        @Test
        @DisplayName("should throw when association not found")
        void createTestEntity_shouldThrow_whenNoAssociation() {
            TestEntity input = new TestEntity();
            input.setDoctorId(2L);
            input.setHospitalId(20L);
            input.setPatientId(200L);

            given(hospitalClient.existHospitalDoctorByDoctorIdAndHospitalId(2L, 20L)).willReturn(false);

            assertThatThrownBy(() -> testService.createTestEntity(input))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active association found with Doctor Id: 2 and Hospital Id: 20");
        }

        @Test
        @DisplayName("should throw when patient not found")
        void createTestEntity_shouldThrow_whenPatientNotFound() {
            TestEntity input = new TestEntity();
            input.setDoctorId(3L);
            input.setHospitalId(30L);
            input.setPatientId(300L);

            given(hospitalClient.existHospitalDoctorByDoctorIdAndHospitalId(3L, 30L)).willReturn(true);
            given(patientClient.existPatientById(300L)).willReturn(true);

            assertThatThrownBy(() -> testService.createTestEntity(input))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active patient found with Patient Id: 300");
        }
    }

    @Nested
    @DisplayName("Get All Tests")
    class GetAllTests {
        @Test
        @DisplayName("should return list of TestEntities")
        void getAllTests_shouldReturnList() {
            TestEntity entity = new TestEntity();
            entity.setId("id1");

            given(testRepository.findByDeletedAtIsNull()).willReturn(List.of(entity));

            List<TestEntity> result = testService.getAllTests();

            assertThat(result).isNotEmpty().hasSize(1).extracting(TestEntity::getId).containsExactly("id1");
            verify(testRepository).findByDeletedAtIsNull();
        }
    }

    @Nested
    @DisplayName("Get TestEntity by ID")
    class GetTestByIdTests {
        @Test
        @DisplayName("should return TestEntity when exists")
        void getTestById_shouldReturn_whenExists() {
            String id = "test1";
            TestEntity entity = new TestEntity();
            entity.setId(id);

            given(testRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(entity));

            TestEntity result = testService.getTestById(id);

            assertThat(result).isNotNull().extracting(TestEntity::getId).isEqualTo(id);
            verify(testRepository).findByIdAndDeletedAtIsNull(id);
        }

        @Test
        @DisplayName("should throw when not found")
        void getTestById_shouldThrow_whenNotFound() {
            String id = "missing";
            given(testRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> testService.getTestById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active test found with ID: " + id);
        }
    }

    @Nested
    @DisplayName("Get PatientTest by Passcode")
    class GetPatientTestByPasscodeTests {
        @Test
        @DisplayName("should throw when passcode format invalid")
        void getPatientTestByPasscode_shouldThrow_whenInvalidFormat() {
            String badCode = "SHORT";
            assertThatThrownBy(() -> testService.getPatientTestByPasscode(badCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid test passcode format");
        }

        @Test
        @DisplayName("should throw when no test found")
        void getPatientTestByPasscode_shouldThrow_whenNotFound() {
            String code = "ABCDEFGHIJ";
            given(testRepository.findByPassCodeAndDeletedAtIsNull(code)).willReturn(Optional.empty());

            assertThatThrownBy(() -> testService.getPatientTestByPasscode(code))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active test found with code: " + code);
        }

        @Test
        @DisplayName("should return PatientTestDTO with nested data when found")
        void getPatientTestByPasscode_shouldReturnDTO_whenFound() {
            String code = "1234567890";
            TestEntity test = new TestEntity();
            test.setId("t1");
            test.setDoctorId(1L);
            test.setPatientId(2L);
            test.setHospitalId(3L);
            test.setPassCode(code);
            test.setCreatedAt(Instant.now());

            ImageStack stack = new ImageStack();
            stack.setId("s1");
            stack.setStackName("Stack1");
            stack.setTestId(test.getId());

            ImageFile file = new ImageFile();
            file.setId("f1");
            file.setFileUrl("url1");
            file.setImageStackId(stack.getId());

            FileAttachment att = new FileAttachment();
            att.setId("a1");
            att.setFileName("file1");
            att.setFileUrl("urlA");
            att.setTestId(test.getId());

            given(testRepository.findByPassCodeAndDeletedAtIsNull(code)).willReturn(Optional.of(test));
            given(imageStackRepository.findByTestIdAndDeletedAtIsNull(test.getId()))
                    .willReturn(List.of(stack));
            given(imageFileRepository.findByImageStackIdAndDeletedAtIsNull(stack.getId()))
                    .willReturn(List.of(file));
            given(fileAttachmentRepository.findByTestIdAndDeletedAtIsNull(test.getId()))
                    .willReturn(List.of(att));

            PatientTestDTO dto = testService.getPatientTestByPasscode(code);

            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(test.getId());
            assertThat(dto.getDoctorId()).isEqualTo(test.getDoctorId());
            assertThat(dto.getPatientId()).isEqualTo(test.getPatientId());
            assertThat(dto.getHospitalId()).isEqualTo(test.getHospitalId());
            assertThat(dto.getPassCode()).isEqualTo(code);
            assertThat(dto.getImageStacks()).hasSize(1);
            ImageStackDTO stackDTO = dto.getImageStacks().get(0);
            assertThat(stackDTO.getId()).isEqualTo(stack.getId());
            assertThat(stackDTO.getImageFiles()).hasSize(1);
            ImageFileDTO fileDTO = stackDTO.getImageFiles().get(0);
            assertThat(fileDTO.getId()).isEqualTo(file.getId());
            assertThat(dto.getFileAttachments()).hasSize(1);
            FileAttachmentDTO attDTO = dto.getFileAttachments().get(0);
            assertThat(attDTO.getId()).isEqualTo(att.getId());
        }
    }

    @Nested
    @DisplayName("Update TestEntity")
    class UpdateTestEntityTests {
        @Test
        @DisplayName("should update fields when exists")
        void updateTestEntity_shouldUpdate_whenValid() {
            String id = "u1";
            TestEntity existing = new TestEntity();
            existing.setId(id);
            existing.setDoctorId(1L);
            existing.setPatientId(5L);
            existing.setHospitalId(6L);

            TestEntity update = new TestEntity();
            update.setPatientId(50L);
            update.setHospitalId(60L);

            given(testRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(existing));
            given(testRepository.save(any(TestEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(patientClient.existPatientById(50L)).willReturn(true);
            given(hospitalClient.existHospitalDoctorByDoctorIdAndHospitalId(1L, 60L)).willReturn(true);

            TestEntity result = testService.updateTestEntity(id, update);

            assertThat(result.getPatientId()).isEqualTo(50L);
            assertThat(result.getHospitalId()).isEqualTo(60L);
            verify(testRepository).save(existing);
        }

        @Test
        @DisplayName("should throw when test not found")
        void updateTestEntity_shouldThrow_whenTestNotFound() {
            String id = "missing";
            TestEntity update = new TestEntity();
            given(testRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> testService.updateTestEntity(id, update))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("TestEntity not found");
        }

        @Test
        @DisplayName("should throw when patient not found")
        void updateTestEntity_shouldThrow_whenPatientNotFound() {
            String id = "u1";
            TestEntity existing = new TestEntity();

            Long patientId = 1L;
            TestEntity update = new TestEntity();
            update.setPatientId(patientId);
            given(testRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(existing));
            given(patientClient.existPatientById(patientId)).willReturn(false);

            assertThatThrownBy(() -> testService.updateTestEntity(id, update))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No active patient found with Patient Id: 1");
        }

        @Test
        @DisplayName("should throw when hospital not found")
        void updateTestEntity_shouldThrow_whenHospitalNotFound() {
            String id = "u1";
            Long doctorId = 1L;
            TestEntity existing = new TestEntity();
            existing.setDoctorId(doctorId);
            Long hospitalId = 1L;

            TestEntity update = new TestEntity();
            update.setHospitalId(hospitalId);
            given(testRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(existing));
            given(hospitalClient.existHospitalDoctorByDoctorIdAndHospitalId(doctorId, hospitalId)).willReturn(false);

            assertThatThrownBy(() -> testService.updateTestEntity(id, update))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No active association found with Doctor Id: 1 and Hospital Id: 1");
        }
    }

    @Nested
    @DisplayName("Delete TestEntity")
    class DeleteTestEntityTests {
        @Test
        @DisplayName("should set deletedAt when exists")
        void deleteTestEntity_shouldSetDeletedAt_whenExists() {
            String id = "d1";
            TestEntity existing = new TestEntity();
            existing.setId(id);

            given(testRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(existing));
            given(testRepository.save(any(TestEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

            testService.deleteTestEntity(id);

            assertThat(existing.getDeletedAt()).isNotNull();
            verify(testRepository).save(existing);
        }

        @Test
        @DisplayName("should throw when not found")
        void deleteTestEntity_shouldThrow_whenNotFound() {
            String id = "missing";
            given(testRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> testService.deleteTestEntity(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cannot delete. No active test found with ID: " + id);
        }
    }
}

