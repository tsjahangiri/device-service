package com.device.management.device_service.rest;

import com.device.management.device_service.model.DeviceDTO;
import com.device.management.device_service.service.DeviceService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/devices", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeviceResource {

    private final DeviceService deviceService;

    public DeviceResource(final DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getAllDevices() {
        return ResponseEntity.ok(deviceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(deviceService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createDevice(@RequestBody @Valid final DeviceDTO deviceDTO) {
        final Long createdId = deviceService.create(deviceDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateDevice(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final DeviceDTO deviceDTO) {
        deviceService.update(id, deviceDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteDevice(@PathVariable(name = "id") final Long id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
