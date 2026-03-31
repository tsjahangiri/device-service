package com.device.management.device_service.service;

import com.device.management.device_service.domain.DeviceEntity;
import com.device.management.device_service.dto.State;
import com.device.management.device_service.dto.request.DevicePatchRequest;
import com.device.management.device_service.dto.request.DeviceRequest;
import com.device.management.device_service.dto.response.DeviceResponse;
import com.device.management.device_service.exception.DeviceNotDeletableException;
import com.device.management.device_service.exception.DeviceNotFoundException;
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
        final DeviceRequest request = buildValidRequest();
        final DeviceEntity entity = buildEntity(State.AVAILABLE);
        final UUID deviceId = UUID.randomUUID();
        final DeviceResponse expectedResponse = buildValidResponse(deviceId);

        when(deviceMapper.toDeviceEntity(request)).thenReturn(entity);
        when(deviceRepository.save(entity)).thenReturn(entity);
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(expectedResponse);

        final DeviceResponse result = deviceService.createDevice(request);

        verify(deviceRepository).save(entity);
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.getDeviceId()).isEqualTo(deviceId);
    }

    // ─── GET SINGLE ────────────────────────────────────────────────────────

    @Test
    void getDevice_existingDevice_returnsResponse() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceEntity entity = buildEntity(deviceId, State.AVAILABLE);
        final DeviceResponse expectedResponse = buildValidResponse(deviceId);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(expectedResponse);

        final DeviceResponse result = deviceService.getDevice(deviceId);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    void getDevice_nonExistentDevice_throwsDeviceNotFoundException() {
        final UUID deviceId = UUID.randomUUID();

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getDevice(deviceId))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining(deviceId.toString());
    }

    // ─── GET ALL / FILTER ──────────────────────────────────────────────────

    @Test
    void getDevices_noBrandNoState_returnsAllDevices() {
        final UUID deviceId1 = UUID.randomUUID();
        final UUID deviceId2 = UUID.randomUUID();

        final DeviceEntity entity1 = buildEntity(deviceId1, State.AVAILABLE);
        final DeviceEntity entity2 = buildEntity(deviceId2, State.IN_USE);

        final DeviceResponse response1 = buildValidResponse(deviceId1);
        final DeviceResponse response2 = buildValidResponse(deviceId2);

        when(deviceRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(deviceMapper.toDeviceResponse(entity1)).thenReturn(response1);
        when(deviceMapper.toDeviceResponse(entity2)).thenReturn(response2);

        final List<DeviceResponse> result = deviceService.getDevices(null, null);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(response1, response2);
        verify(deviceRepository).findAll();
        verify(deviceRepository, never()).findByBrand(any());
        verify(deviceRepository, never()).findByState(any());
    }

    @Test
    void getDevices_withBrand_returnsFilteredByBrand() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceEntity entity = buildEntity(deviceId, State.AVAILABLE);
        final DeviceResponse response = buildValidResponse(deviceId);

        when(deviceRepository.findByBrand("Apple")).thenReturn(List.of(entity));
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(response);

        final List<DeviceResponse> result = deviceService.getDevices("Apple", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeviceId()).isEqualTo(deviceId);
        verify(deviceRepository).findByBrand("Apple");
        verify(deviceRepository, never()).findAll();
        verify(deviceRepository, never()).findByState(any());
    }

    @Test
    void getDevices_withState_returnsFilteredByState() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceEntity entity = buildEntity(deviceId, State.AVAILABLE);
        final DeviceResponse response = buildValidResponse(deviceId);

        when(deviceRepository.findByState(State.AVAILABLE)).thenReturn(List.of(entity));
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(response);

        final List<DeviceResponse> result = deviceService.getDevices(null, State.AVAILABLE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeviceId()).isEqualTo(deviceId);
        verify(deviceRepository).findByState(State.AVAILABLE);
        verify(deviceRepository, never()).findAll();
        verify(deviceRepository, never()).findByBrand(any());
    }

    @Test
    void getDevices_withBrandAndState_brandTakesPrecedence() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceEntity entity = buildEntity(deviceId, State.AVAILABLE);
        final DeviceResponse response = buildValidResponse(deviceId);

        when(deviceRepository.findByBrand("Apple")).thenReturn(List.of(entity));
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(response);

        final List<DeviceResponse> result = deviceService.getDevices("Apple", State.AVAILABLE);

        assertThat(result).hasSize(1);
        verify(deviceRepository).findByBrand("Apple");
        verify(deviceRepository, never()).findByState(any());
        verify(deviceRepository, never()).findAll();
    }

    // ─── FULL UPDATE ───────────────────────────────────────────────────────

    @Test
    void updateDevice_availableDevice_updatesSuccessfully() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceRequest request = buildValidRequest();
        final DeviceEntity entity = buildEntity(deviceId, State.AVAILABLE);
        final DeviceResponse expectedResponse = buildValidResponse(deviceId);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));
        when(deviceRepository.save(entity)).thenReturn(entity);
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(expectedResponse);

        final DeviceResponse result = deviceService.updateDevice(deviceId, request);

        verify(deviceMapper).updateDeviceEntity(request, entity);
        verify(deviceRepository).save(entity);
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void updateDevice_deviceInUse_throwsDeviceNotUpdatableException() {
        final UUID deviceId = UUID.randomUUID();
        final DeviceRequest request = buildValidRequest();
        final DeviceEntity entity = buildEntity(deviceId, State.IN_USE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> deviceService.updateDevice(deviceId, request))
                .isInstanceOf(DeviceNotUpdatableException.class)
                .hasMessageContaining(deviceId.toString());

        verify(deviceRepository, never()).save(any());
    }

    @Test
    void updateDevice_nonExistentDevice_throwsDeviceNotFoundException() {
        final UUID deviceId = UUID.randomUUID();

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.updateDevice(deviceId, buildValidRequest()))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining(deviceId.toString());
    }

    // ─── PARTIAL UPDATE ────────────────────────────────────────────────────

    @Test
    void patchDevice_stateOnly_updatesSuccessfully() {
        final UUID deviceId = UUID.randomUUID();
        final DevicePatchRequest request = buildPatchRequestWithState(State.IN_USE);
        final DeviceEntity entity = buildEntity(deviceId, State.AVAILABLE);
        final DeviceResponse expectedResponse = buildValidResponse(deviceId);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));
        when(deviceRepository.save(entity)).thenReturn(entity);
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(expectedResponse);

        deviceService.patchDevice(deviceId, request);

        verify(deviceRepository).save(entity);
        assertThat(entity.getState()).isEqualTo(State.IN_USE);
    }

    @Test
    void patchDevice_nameChangeOnInUseDevice_throwsDeviceNotUpdatableException() {
        final UUID deviceId = UUID.randomUUID();
        final DevicePatchRequest request = buildPatchRequestWithName("New Name");
        final DeviceEntity entity = buildEntity(deviceId, State.IN_USE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> deviceService.patchDevice(deviceId, request))
                .isInstanceOf(DeviceNotUpdatableException.class)
                .hasMessageContaining(deviceId.toString());

        verify(deviceRepository, never()).save(any());
    }

    @Test
    void patchDevice_brandChangeOnInUseDevice_throwsDeviceNotUpdatableException() {
        final UUID deviceId = UUID.randomUUID();
        final DevicePatchRequest request = buildPatchRequestWithBrand("Samsung");
        final DeviceEntity entity = buildEntity(deviceId, State.IN_USE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> deviceService.patchDevice(deviceId, request))
                .isInstanceOf(DeviceNotUpdatableException.class)
                .hasMessageContaining(deviceId.toString());

        verify(deviceRepository, never()).save(any());
    }

    @Test
    void patchDevice_stateChangeOnInUseDevice_updatesSuccessfully() {
        final UUID deviceId = UUID.randomUUID();
        final DevicePatchRequest request = buildPatchRequestWithState(State.INACTIVE);
        final DeviceEntity entity = buildEntity(deviceId, State.IN_USE);
        final DeviceResponse expectedResponse = buildValidResponse(deviceId);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(entity));
        when(deviceRepository.save(entity)).thenReturn(entity);
        when(deviceMapper.toDeviceResponse(entity)).thenReturn(expectedResponse);

        deviceService.patchDevice(deviceId, request);

        verify(deviceRepository).save(entity);
        assertThat(entity.getState()).isEqualTo(State.INACTIVE);
    }

    @Test
    void patchDevice_nonExistentDevice_throwsDeviceNotFoundException() {
        final UUID deviceId = UUID.randomUUID();
        final DevicePatchRequest request = buildPatchRequestWithState(State.AVAILABLE);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.patchDevice(deviceId, request))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining(deviceId.toString());
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
                .isInstanceOf(DeviceNotDeletableException.class)
                .hasMessageContaining(deviceId.toString());

        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void deleteDevice_nonExistentDevice_throwsDeviceNotFoundException() {
        final UUID deviceId = UUID.randomUUID();

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.deleteDevice(deviceId))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining(deviceId.toString());
    }
}
