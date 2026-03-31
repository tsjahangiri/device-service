package com.device.management.device_service.repository;

import com.device.management.device_service.config.DomainConfig;
import com.device.management.device_service.domain.DeviceEntity;
import com.device.management.device_service.dto.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(DomainConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DeviceRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    void cleanUp() {
        deviceRepository.deleteAll();
    }

    @Test
    void findByDeviceId_existingDevice_returnsDevice() {
        final DeviceEntity saved = saveDevice("iPhone 15 Pro", "Apple", State.AVAILABLE);

        final Optional<DeviceEntity> result = deviceRepository.findByDeviceId(saved.getDeviceId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("iPhone 15 Pro");
    }

    @Test
    void findByDeviceId_nonExistentDevice_returnsEmpty() {
        final Optional<DeviceEntity> result = deviceRepository.findByDeviceId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findByBrand_returnsOnlyMatchingBrand() {
        saveDevice("iPhone 15 Pro", "Apple", State.AVAILABLE);
        saveDevice("MacBook Pro", "Apple", State.IN_USE);
        saveDevice("Galaxy S24", "Samsung", State.AVAILABLE);

        final List<DeviceEntity> result = deviceRepository.findByBrand("Apple");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(d -> d.getBrand().equals("Apple"));
    }

    @Test
    void findByState_returnsOnlyMatchingState() {
        saveDevice("iPhone 15 Pro", "Apple", State.AVAILABLE);
        saveDevice("MacBook Pro", "Apple", State.IN_USE);
        saveDevice("Galaxy S24", "Samsung", State.AVAILABLE);

        final List<DeviceEntity> result = deviceRepository.findByState(State.AVAILABLE);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(d -> d.getState() == State.AVAILABLE);
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────

    private DeviceEntity saveDevice(final String name, final String brand, final State state) {
        final DeviceEntity entity = new DeviceEntity();
        entity.setName(name);
        entity.setBrand(brand);
        entity.setState(state);
        return deviceRepository.save(entity);
    }
}
