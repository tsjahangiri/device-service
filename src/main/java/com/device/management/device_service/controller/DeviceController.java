package com.device.management.device_service.controller;

import com.device.management.device_service.dto.State;
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

    @GetMapping("/{deviceId}")
    @Operation(summary = "Get a device by ID")
    @ApiResponse(responseCode = "200", description = "Device found")
    @ApiResponse(responseCode = "404", description = "Device not found")
    public ResponseEntity<DeviceResponse> getDevice(
            @PathVariable final UUID deviceId) {
        return ResponseEntity.ok(deviceService.getDevice(deviceId));
    }

    @GetMapping
    @Operation(summary = "Get all devices, optionally filtered by brand or state")
    @ApiResponse(responseCode = "200", description = "Devices retrieved successfully")
    public ResponseEntity<List<DeviceResponse>> getDevices(
            @RequestParam(required = false) final String brand,
            @RequestParam(required = false) final State state) {
        if (brand != null) {
            return ResponseEntity.ok(deviceService.getDevicesByBrand(brand));
        }
        if (state != null) {
            return ResponseEntity.ok(deviceService.getDevicesByState(state));
        }
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Delete a device by ID")
    @ApiResponse(responseCode = "204", description = "Device deleted successfully")
    @ApiResponse(responseCode = "404", description = "Device not found")
    @ApiResponse(responseCode = "409", description = "Device is in use and cannot be deleted")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable final UUID deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }

}
