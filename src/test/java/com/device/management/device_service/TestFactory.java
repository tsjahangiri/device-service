package com.device.management.device_service;

import com.device.management.device_service.domain.DeviceEntity;
import com.device.management.device_service.dto.State;
import com.device.management.device_service.dto.request.DeviceRequest;
import com.device.management.device_service.dto.response.DeviceResponse;

import java.util.UUID;

public final class TestFactory {

    public static DeviceRequest buildRequest(final String name,
                                             final String brand, final State state) {
        final DeviceRequest request = new DeviceRequest();
        request.setName(name);
        request.setBrand(brand);
        request.setState(state);
        return request;
    }

    public static DeviceResponse buildResponse(final UUID deviceId, final String name,
                                         final String brand, final State state) {
        final DeviceResponse response = new DeviceResponse();
        response.setDeviceId(deviceId);
        response.setName(name);
        response.setBrand(brand);
        response.setState(state);
        return response;
    }

    public static DeviceEntity buildEntity(final UUID deviceId, final State state) {
        final DeviceEntity entity = new DeviceEntity();
        entity.setState(state);
        return entity;
    }


}
