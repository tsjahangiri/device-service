package com.device.management.device_service.repository;

import com.device.management.device_service.domain.DeviceEntity;
import com.device.management.device_service.dto.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {

    Optional<DeviceEntity> findByDeviceId(UUID deviceId);

    List<DeviceEntity> findByBrand(String brand);

    List<DeviceEntity> findByState(State state);
}
