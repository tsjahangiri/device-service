package com.device.management.device_service.service;

import com.device.management.device_service.domain.DeviceEntity;
import com.device.management.device_service.dto.State;
import com.device.management.device_service.dto.request.DevicePatchRequest;
import com.device.management.device_service.dto.request.DeviceRequest;
import com.device.management.device_service.dto.response.DeviceResponse;
import com.device.management.device_service.exception.DeviceNotFoundException;
import com.device.management.device_service.exception.DeviceNotDeletableException;
import com.device.management.device_service.exception.DeviceNotUpdatableException;
import com.device.management.device_service.repository.DeviceRepository;
import com.device.management.device_service.transform.DeviceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.device.management.device_service.TestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceService deviceService;

    // ─── CREATE ────────────────────────────────────────────────────────────

    @Test
    void createDevice_validRequest_savesAndReturnsResponse() {
        final DeviceRequest request = buildRequest("iPhone 15 Pro", "Apple", State.AVAILABLE);
        final DeviceEntity entity = new DeviceEntity();
        final DeviceResponse expectedResponse = buildResponse(UUID.randomUUID(), "iPhone 15 Pro", "Apple", State.AVAILABLE);

        when(deviceMapper.toDeviceEntity(request)).thenReturn(entity);
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(expectedResponse);

        final DeviceResponse result = deviceService.createDevice(request);

        verify(deviceRepository).save(entity);
        assertThat(result).isEqualTo(expectedResponse);
    }

    // ─── GET SINGLE ────────────────────────────────────────────────────────

    @Test
    void getDevice_existingDevice_returnsResponse() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceEntity entity = new DeviceEntity();
        final DeviceResponse expectedResponse = buildResponse(deviceId, "iPhone 15 Pro", "Apple", State.AVAILABLE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(expectedResponse);

        final DeviceResponse result = deviceService.getDevice(deviceId);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getDevice_nonExistentDevice_throwsDeviceNotFoundException() {
        final UUID deviceId = UUID.randomUUID();
        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getDevice(deviceId))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    // ─── GET ALL / FILTER ──────────────────────────────────────────────────

    @Test
    void getAllDevices_returnsAllDevices() {
        final DeviceEntity entity1 = new DeviceEntity();
        final DeviceEntity entity2 = new DeviceEntity();
        when(deviceRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(deviceMapper.toDeviceResponse(any())).thenReturn(new DeviceResponse());

        final List<DeviceResponse> result = deviceService.getAllDevices();

        assertThat(result).hasSize(2);
    }

    @Test
    void getDevicesByBrand_returnsFilteredDevices() {
        final DeviceEntity entity = new DeviceEntity();
        when(deviceRepository.findByBrand("Apple")).thenReturn(List.of(entity));
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(new DeviceResponse());

        final List<DeviceResponse> result = deviceService.getDevicesByBrand("Apple");

        assertThat(result).hasSize(1);
    }

    @Test
    void getDevicesByState_returnsFilteredDevices() {
        final DeviceEntity entity = new DeviceEntity();
        when(deviceRepository.findByState(State.AVAILABLE)).thenReturn(List.of(entity));
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(new DeviceResponse());

        final List<DeviceResponse> result = deviceService.getDevicesByState(State.AVAILABLE);

        assertThat(result).hasSize(1);
    }

    // ─── FULL UPDATE ───────────────────────────────────────────────────────

    @Test
    void updateDevice_availableDevice_updatesSuccessfully() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceRequest request = buildRequest("iPhone 16 Pro", "Apple", State.AVAILABLE);
        final DeviceEntity entity = buildEntity(deviceId, State.AVAILABLE);
        final DeviceResponse expectedResponse = buildResponse(deviceId, "iPhone 16 Pro", "Apple", State.AVAILABLE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(expectedResponse);

        final DeviceResponse result = deviceService.updateDevice(deviceId, request);

        verify(deviceMapper).updateDeviceEntity(request, entity);
        verify(deviceRepository).save(entity);
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void updateDevice_deviceInUse_throwsDeviceNotUpdatableException() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceRequest request = buildRequest("iPhone 16 Pro", "Apple", State.IN_USE);
        final DeviceEntity entity = buildEntity(deviceId, State.IN_USE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> deviceService.updateDevice(deviceId, request))
                .isInstanceOf(DeviceNotUpdatableException.class);

        verify(deviceRepository, never()).save(any());
    }

    @Test
    void updateDevice_nonExistentDevice_throwsDeviceNotFoundException() {
        final UUID deviceId = UUID.randomUUID();
        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.updateDevice(deviceId, buildRequest("name", "brand", State.AVAILABLE)))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    // ─── PARTIAL UPDATE ────────────────────────────────────────────────────

    @Test
    void patchDevice_stateOnly_updatesSuccessfully() {
        final UUID deviceId = UUID.randomUUID();
        final DevicePatchRequest request = new DevicePatchRequest();
        request.setState(State.IN_USE);
        final DeviceEntity entity = buildEntity(deviceId, State.AVAILABLE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(new DeviceResponse());

        deviceService.patchDevice(deviceId, request);

        verify(deviceRepository).save(entity);
        assertThat(entity.getState()).isEqualTo(State.IN_USE);
    }

    @Test
    void patchDevice_nameChangeOnInUseDevice_throwsDeviceNotUpdatableException() {
        final UUID deviceId = UUID.randomUUID();
        final DevicePatchRequest request = new DevicePatchRequest();
        request.setName("New Name");
        final DeviceEntity entity = buildEntity(deviceId, State.IN_USE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> deviceService.patchDevice(deviceId, request))
                .isInstanceOf(DeviceNotUpdatableException.class);

        verify(deviceRepository, never()).save(any());
    }

    @Test
    void patchDevice_stateChangeOnInUseDevice_updatesSuccessfully() {
        final UUID deviceId = UUID.randomUUID();
        final DevicePatchRequest request = new DevicePatchRequest();
        request.setState(State.INACTIVE);
        final DeviceEntity entity = buildEntity(deviceId, State.IN_USE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(new DeviceResponse());

        deviceService.patchDevice(deviceId, request);

        verify(deviceRepository).save(entity);
        assertThat(entity.getState()).isEqualTo(State.INACTIVE);
    }

    // ─── DELETE ────────────────────────────────────────────────────────────

    @Test
    void deleteDevice_availableDevice_deletesSuccessfully() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceEntity entity = buildEntity(deviceId, State.AVAILABLE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));

        deviceService.deleteDevice(deviceId);

        verify(deviceRepository).delete(entity);
    }

    @Test
    void deleteDevice_inUseDevice_throwsDeviceNotDeletableException() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceEntity entity = buildEntity(deviceId, State.IN_USE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> deviceService.deleteDevice(deviceId))
                .isInstanceOf(DeviceNotDeletableException.class);

        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void deleteDevice_nonExistentDevice_throwsDeviceNotFoundException() {
        final UUID deviceId = UUID.randomUUID();
        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.deleteDevice(deviceId))
                .isInstanceOf(DeviceNotFoundException.class);
    }

}

