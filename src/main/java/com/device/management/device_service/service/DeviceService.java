package com.device.management.device_service.service;

import com.device.management.device_service.domain.DeviceEntity;
import com.device.management.device_service.dto.State;
import com.device.management.device_service.dto.request.DevicePatchRequest;
import com.device.management.device_service.dto.request.DeviceRequest;
import com.device.management.device_service.dto.response.DeviceResponse;
import com.device.management.device_service.exception.DeviceNotFoundException;
import com.device.management.device_service.exception.DeviceNotUpdatableException;
import com.device.management.device_service.repository.DeviceRepository;
import com.device.management.device_service.transform.DeviceMapper;
import com.device.management.device_service.util.NotFoundException;
import org.springframework.data.domain.Sort;
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
        this.deviceRepository.save(deviceEntity);
        return this.deviceMapper.toDeviceResponse(deviceEntity);
    }

    public DeviceResponse updateDevice(final UUID deviceId, final DeviceRequest deviceRequest) {
        final DeviceEntity deviceEntity = this.deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        if (deviceEntity.getState() == State.IN_USE) {
            throw new DeviceNotUpdatableException(deviceId);
        }

        this.deviceMapper.updateDeviceEntity(deviceRequest, deviceEntity);
        this.deviceRepository.save(deviceEntity);
        return this.deviceMapper.toDeviceResponse(deviceEntity);
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

        this.deviceRepository.save(deviceEntity);
        return this.deviceMapper.toDeviceResponse(deviceEntity);
    }

    //////// to be refined

    public List<DeviceRequest> findAll() {
        final List<DeviceEntity> deviceEntities = this.deviceRepository.findAll(Sort.by("id"));
        return deviceEntities.stream()
                .map(deviceEntity -> mapToDTO(deviceEntity, new DeviceRequest()))
                .toList();
    }

    public DeviceRequest get(final Long id) {
        return this.deviceRepository.findById(id)
                .map(deviceEntity -> mapToDTO(deviceEntity, new DeviceRequest()))
                .orElseThrow(NotFoundException::new);
    }

    public void delete(final Long id) {
        final DeviceEntity deviceEntity = this.deviceRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        this.deviceRepository.delete(deviceEntity);
    }

    private DeviceRequest mapToDTO(final DeviceEntity deviceEntity, final DeviceRequest deviceRequest) {
        deviceRequest.setName(deviceEntity.getName());
        deviceRequest.setBrand(deviceEntity.getBrand());
        deviceRequest.setState(deviceEntity.getState());
        return deviceRequest;
    }

}
