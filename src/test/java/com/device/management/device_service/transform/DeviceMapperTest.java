package com.device.management.device_service.transform;

import com.device.management.device_service.domain.DeviceEntity;
import com.device.management.device_service.dto.State;
import com.device.management.device_service.dto.request.DeviceRequest;
import com.device.management.device_service.dto.response.DeviceResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceMapperTest {

    private final DeviceMapper deviceMapper = Mappers.getMapper(DeviceMapper.class);

    @Test
    void toDeviceEntity_mapsCorrectly() {
        final DeviceRequest request = new DeviceRequest();
        request.setName("iPhone 15 Pro");
        request.setBrand("Apple");
        request.setState(State.AVAILABLE);

        final DeviceEntity entity = deviceMapper.toDeviceEntity(request);

        assertThat(entity.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(entity.getBrand()).isEqualTo("Apple");
        assertThat(entity.getState()).isEqualTo(State.AVAILABLE);
        assertThat(entity.getId()).isNull();
        assertThat(entity.getDeviceId()).isNull();
        assertThat(entity.getDateCreated()).isNull();
        assertThat(entity.getLastUpdated()).isNull();
    }

    @Test
    void toDeviceResponse_mapsCorrectly() {
        final DeviceEntity entity = new DeviceEntity();
        entity.setDeviceId(UUID.randomUUID());
        entity.setName("iPhone 15 Pro");
        entity.setBrand("Apple");
        entity.setState(State.AVAILABLE);

        final DeviceResponse response = deviceMapper.toDeviceResponse(entity);

        assertThat(response.getDeviceId()).isEqualTo(entity.getDeviceId());
        assertThat(response.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(response.getBrand()).isEqualTo("Apple");
        assertThat(response.getState()).isEqualTo(State.AVAILABLE);
    }

    @Test
    void updateDeviceEntity_updatesOnlyMappedFields() {
        final DeviceEntity entity = new DeviceEntity();
        entity.setName("Old Name");
        entity.setBrand("Old Brand");
        entity.setState(State.AVAILABLE);

        final DeviceRequest request = new DeviceRequest();
        request.setName("New Name");
        request.setBrand("New Brand");
        request.setState(State.IN_USE);

        deviceMapper.updateDeviceEntity(request, entity);

        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getBrand()).isEqualTo("New Brand");
        assertThat(entity.getState()).isEqualTo(State.IN_USE);
    }
}

