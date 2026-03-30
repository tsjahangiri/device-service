package com.device.management.device_service.repository;

import com.device.management.device_service.domain.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
}
