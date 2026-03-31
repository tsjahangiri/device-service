package com.device.management.device_service.dto.request;

import com.device.management.device_service.dto.State;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DeviceBaseRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String brand;

    private State state;

}
