package com.device.management.device_service;

import com.device.management.device_service.dto.State;
import com.device.management.device_service.dto.request.DeviceRequest;

public final class TestFactory {

    public static DeviceRequest buildRequest(final String name,
                                             final String brand, final State state) {
        final DeviceRequest request = new DeviceRequest();
        request.setName(name);
        request.setBrand(brand);
        request.setState(state);
        return request;
    }


}
