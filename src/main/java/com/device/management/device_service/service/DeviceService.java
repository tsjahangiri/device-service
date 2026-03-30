package com.device.management.device_service.service;

import com.device.management.device_service.domain.DeviceEntity;
import com.device.management.device_service.dto.request.DeviceRequest;
import com.device.management.device_service.dto.response.DeviceResponse;
import com.device.management.device_service.repository.DeviceRepository;
import com.device.management.device_service.transform.DeviceMapper;
import com.device.management.device_service.util.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


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
        return deviceMapper.toDeviceResponse(deviceEntity);
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

    public void update(final Long id, final DeviceRequest deviceRequest) {
        final DeviceEntity deviceEntity = this.deviceRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(deviceRequest, deviceEntity);
        this.deviceRepository.save(deviceEntity);
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

    private DeviceEntity mapToEntity(final DeviceRequest deviceRequest, final DeviceEntity deviceEntity) {
        deviceEntity.setName(deviceRequest.getName());
        deviceEntity.setBrand(deviceRequest.getBrand());
        deviceEntity.setState(deviceRequest.getState());
        return deviceEntity;
    }

}
