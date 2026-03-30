package com.device.management.device_service.dto.response;

import com.device.management.device_service.dto.State;
import lombok.*;

import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private UUID deviceId;
    private String name;
    private String brand;
    private State state;

}
