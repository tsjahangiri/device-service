package com.device.management.device_service.service;

import com.device.management.device_service.domain.Device;
import com.device.management.device_service.model.DeviceDTO;
import com.device.management.device_service.repos.DeviceRepository;
import com.device.management.device_service.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(final DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<DeviceDTO> findAll() {
        final List<Device> devices = deviceRepository.findAll(Sort.by("id"));
        return devices.stream()
                .map(device -> mapToDTO(device, new DeviceDTO()))
                .toList();
    }

    public DeviceDTO get(final Long id) {
        return deviceRepository.findById(id)
                .map(device -> mapToDTO(device, new DeviceDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final DeviceDTO deviceDTO) {
        final Device device = new Device();
        mapToEntity(deviceDTO, device);
        return deviceRepository.save(device).getId();
    }

    public void update(final Long id, final DeviceDTO deviceDTO) {
        final Device device = deviceRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(deviceDTO, device);
        deviceRepository.save(device);
    }

    public void delete(final Long id) {
        final Device device = deviceRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        deviceRepository.delete(device);
    }

    private DeviceDTO mapToDTO(final Device device, final DeviceDTO deviceDTO) {
        deviceDTO.setId(device.getId());
        deviceDTO.setName(device.getName());
        deviceDTO.setBrand(device.getBrand());
        deviceDTO.setState(device.getState());
        return deviceDTO;
    }

    private Device mapToEntity(final DeviceDTO deviceDTO, final Device device) {
        device.setName(deviceDTO.getName());
        device.setBrand(deviceDTO.getBrand());
        device.setState(deviceDTO.getState());
        return device;
    }

}
