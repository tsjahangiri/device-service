package com.device.management.device_service.repos;

import com.device.management.device_service.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DeviceRepository extends JpaRepository<Device, Long> {
}
