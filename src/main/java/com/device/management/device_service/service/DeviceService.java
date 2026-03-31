package com.device.management.device_service.service;

import com.device.management.device_service.domain.DeviceEntity;
import com.device.management.device_service.dto.State;
import com.device.management.device_service.dto.request.DevicePatchRequest;
import com.device.management.device_service.dto.request.DeviceRequest;
import com.device.management.device_service.dto.response.DeviceResponse;
import com.device.management.device_service.exception.DataPersistenceException;
import com.device.management.device_service.exception.DeviceNotDeletableException;
import com.device.management.device_service.exception.DeviceNotFoundException;
import com.device.management.device_service.exception.DeviceNotUpdatableException;
import com.device.management.device_service.repository.DeviceRepository;
import com.device.management.device_service.transform.DeviceMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public DeviceService(final DeviceRepository deviceRepository, final DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    public DeviceResponse createDevice(final DeviceRequest deviceRequest) {
        DeviceEntity deviceEntity = this.deviceMapper.toDeviceEntity(deviceRequest);
        final DeviceEntity savedEntity = this.saveDevice(deviceEntity);
        return this.deviceMapper.toDeviceResponse(savedEntity);
    }

    public DeviceResponse updateDevice(final UUID deviceId, final DeviceRequest deviceRequest) {
        final DeviceEntity deviceEntity = this.deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        if (deviceEntity.getState() == State.IN_USE) {
            throw new DeviceNotUpdatableException(deviceId);
        }

        this.deviceMapper.updateDeviceEntity(deviceRequest, deviceEntity);
        final DeviceEntity savedEntity = this.saveDevice(deviceEntity);
        return this.deviceMapper.toDeviceResponse(savedEntity);
    }

    public DeviceResponse patchDevice(final UUID deviceId, final DevicePatchRequest deviceRequest) {
        final DeviceEntity deviceEntity = this.deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        if (deviceEntity.getState() == State.IN_USE) {
            if (deviceRequest.getName() != null || deviceRequest.getBrand() != null) {
                throw new DeviceNotUpdatableException(deviceId);
            }
        }

        if (deviceRequest.getName() != null) {
            deviceEntity.setName(deviceRequest.getName());
        }
        if (deviceRequest.getBrand() != null) {
            deviceEntity.setBrand(deviceRequest.getBrand());
        }
        if (deviceRequest.getState() != null) {
            deviceEntity.setState(deviceRequest.getState());
        }

        final DeviceEntity savedEntity = this.saveDevice(deviceEntity);
        return this.deviceMapper.toDeviceResponse(savedEntity);
    }

    public DeviceResponse getDevice(final UUID deviceId) {
        return this.deviceRepository.findByDeviceId(deviceId)
                .map(this.deviceMapper::toDeviceResponse)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));
    }

    public List<DeviceResponse> getDevices(final String brand, final State state) {
        if (brand != null) {
            return getDevicesByBrand(brand);
        }
        if (state != null) {
            return getDevicesByState(state);
        }
        return getAllDevices();
    }

    public void deleteDevice(final UUID deviceId) {
        final DeviceEntity deviceEntity = this.deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        if (deviceEntity.getState() == State.IN_USE) {
            throw new DeviceNotDeletableException(deviceId);
        }

        this.deleteDevice(deviceEntity);
    }

    // ─── Private helpers ───────────────────────────────────────────────────

    private List<DeviceResponse> getAllDevices() {
        return this.deviceRepository.findAll()
                .stream()
                .map(this.deviceMapper::toDeviceResponse)
                .toList();
    }

    private List<DeviceResponse> getDevicesByBrand(final String brand) {
        return this.deviceRepository.findByBrand(brand)
                .stream()
                .map(this.deviceMapper::toDeviceResponse)
                .toList();
    }

    private List<DeviceResponse> getDevicesByState(final State state) {
        return this.deviceRepository.findByState(state)
                .stream()
                .map(this.deviceMapper::toDeviceResponse)
                .toList();
    }

    private DeviceEntity saveDevice(final DeviceEntity deviceEntity) {
        try {
            return this.deviceRepository.save(deviceEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new DataPersistenceException("Device could not be saved due to a data integrity violation");
        } catch (DataAccessException ex) {
            throw new DataPersistenceException("Device could not be saved due to a database error");
        }
    }

    private void deleteDevice(final DeviceEntity deviceEntity) {
        try {
            this.deviceRepository.delete(deviceEntity);
        } catch (DataAccessException ex) {
            throw new DataPersistenceException(
                    "Device could not be deleted due to a database error");
        }
    }

}
