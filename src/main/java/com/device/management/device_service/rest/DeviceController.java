package com.device.management.device_service.rest;

import com.device.management.device_service.dto.request.DevicePatchRequest;
import com.device.management.device_service.dto.request.DeviceRequest;
import com.device.management.device_service.dto.response.DeviceResponse;
import com.device.management.device_service.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/devices", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(final DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @Operation(summary = "Create a new device")
    @ApiResponse(responseCode = "201", description = "Device created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<DeviceResponse> createDevice(@RequestBody @Valid final DeviceRequest deviceRequest) {
        final DeviceResponse response = deviceService.createDevice(deviceRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{deviceId}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable final UUID deviceId,
            @RequestBody @Valid final DeviceRequest request) {
        return ResponseEntity.ok(deviceService.updateDevice(deviceId, request));
    }

    @PatchMapping("/{deviceId}")
    public ResponseEntity<DeviceResponse> patchDevice(
            @PathVariable final UUID deviceId,
            @RequestBody final DevicePatchRequest request) {
        return ResponseEntity.ok(deviceService.patchDevice(deviceId, request));
    }


    ////// to be refined
    @GetMapping
    public ResponseEntity<List<DeviceRequest>> getAllDevices() {
        return ResponseEntity.ok(deviceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceRequest> getDevice(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(deviceService.get(id));
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteDevice(@PathVariable(name = "id") final Long id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
