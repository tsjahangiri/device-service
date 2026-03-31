package com.device.management.device_service.controller;

import com.device.management.device_service.BaseIntegrationTest;
import com.device.management.device_service.dto.State;
import com.device.management.device_service.dto.request.DevicePatchRequest;
import com.device.management.device_service.dto.request.DeviceRequest;
import com.device.management.device_service.dto.response.DeviceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static com.device.management.device_service.TestFactory.buildRequest;
import static org.assertj.core.api.Assertions.assertThat;

public class DeviceControllerIT extends BaseIntegrationTest {

    // ─── CREATE DEVICE ────────────────────────────────────────────────────────────

    @Test
    void createDevice_validRequest_returns201() {
        final DeviceRequest request = buildRequest("iPhone 15 Pro", "Apple", State.AVAILABLE);

        final ResponseEntity<DeviceResponse> response = restTemplate
                .postForEntity(baseUrl(), request, DeviceResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDeviceId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("iPhone 15 Pro");
        assertThat(response.getBody().getBrand()).isEqualTo("Apple");
        assertThat(response.getBody().getState()).isEqualTo(State.AVAILABLE);
    }

    @Test
    void createDevice_missingName_returns400() {
        final DeviceRequest request = buildRequest(null, "Apple", State.AVAILABLE);

        final ResponseEntity<Void> response = restTemplate
                .postForEntity(baseUrl(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createDevice_missingBrand_returns400() {
        final DeviceRequest request = buildRequest("iPhone 15 Pro", null, State.AVAILABLE);

        final ResponseEntity<Void> response = restTemplate
                .postForEntity(baseUrl(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createDevice_missingState_returns400() {
        final DeviceRequest request = buildRequest("iPhone 15 Pro", "Apple", null);

        final ResponseEntity<Void> response = restTemplate
                .postForEntity(baseUrl(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── GET SINGLE DEVICE ────────────────────────────────────────────────────────

    @Test
    void getDevice_existingDevice_returns200() {
        final UUID deviceId = createDevice("MacBook Pro", "Apple", State.AVAILABLE);

        final ResponseEntity<DeviceResponse> response = restTemplate
                .getForEntity(baseUrl() + "/" + deviceId, DeviceResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    void getDevice_nonExistentDevice_returns404() {
        final ResponseEntity<Void> response = restTemplate
                .getForEntity(baseUrl() + "/" + UUID.randomUUID(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ─── GET ALL / FILTER DEVICES ──────────────────────────────────────────────────

    @Test
    void getAllDevices_returnsAllDevices() {
        createDevice("iPhone 15 Pro", "Apple", State.AVAILABLE);
        createDevice("Galaxy S24", "Samsung", State.IN_USE);

        final ResponseEntity<DeviceResponse[]> response = restTemplate
                .getForEntity(baseUrl(), DeviceResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getDevicesByBrand_returnsFilteredDevices() {
        createDevice("iPhone 15 Pro", "Apple", State.AVAILABLE);
        createDevice("MacBook Pro", "Apple", State.IN_USE);
        createDevice("Galaxy S24", "Samsung", State.AVAILABLE);

        final ResponseEntity<DeviceResponse[]> response = restTemplate
                .getForEntity(baseUrl() + "?brand=Apple", DeviceResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody())
                .allMatch(d -> d.getBrand().equals("Apple"));
    }

    @Test
    void getDevicesByState_returnsFilteredDevices() {
        createDevice("iPhone 15 Pro", "Apple", State.AVAILABLE);
        createDevice("MacBook Pro", "Apple", State.IN_USE);
        createDevice("Galaxy S24", "Samsung", State.AVAILABLE);

        final ResponseEntity<DeviceResponse[]> response = restTemplate
                .getForEntity(baseUrl() + "?state=AVAILABLE", DeviceResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody())
                .allMatch(d -> d.getState() == State.AVAILABLE);
    }

    // ─── FULL UPDATE OF DEVICES ───────────────────────────────────────────────────────

    @Test
    void updateDevice_validRequest_returns200() {
        final UUID deviceId = createDevice("iPhone 15 Pro", "Apple", State.AVAILABLE);
        final DeviceRequest request = buildRequest("iPhone 16 Pro", "Apple", State.AVAILABLE);

        final ResponseEntity<DeviceResponse> response = restTemplate.exchange(
                baseUrl() + "/" + deviceId,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                DeviceResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("iPhone 16 Pro");
    }

    @Test
    void updateDevice_deviceInUse_returns409() {
        final UUID deviceId = createDevice("iPhone 15 Pro", "Apple", State.IN_USE);
        final DeviceRequest request = buildRequest("iPhone 16 Pro", "Apple", State.IN_USE);

        final ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + deviceId,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateDevice_nonExistentDevice_returns404() {
        final DeviceRequest request = buildRequest("iPhone 16 Pro", "Apple", State.AVAILABLE);

        final ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + UUID.randomUUID(),
                HttpMethod.PUT,
                new HttpEntity<>(request),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ─── PARTIAL UPDATE OF DEVICES ────────────────────────────────────────────────────

    @Test
    void patchDevice_stateOnly_returns200() {
        final UUID deviceId = createDevice("iPhone 15 Pro", "Apple", State.AVAILABLE);
        final DevicePatchRequest request = new DevicePatchRequest();
        request.setState(State.IN_USE);

        final ResponseEntity<DeviceResponse> response = restTemplate.exchange(
                baseUrl() + "/" + deviceId,
                HttpMethod.PATCH,
                new HttpEntity<>(request),
                DeviceResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getState()).isEqualTo(State.IN_USE);
    }

    @Test
    void patchDevice_nameChangeOnInUseDevice_returns409() {
        final UUID deviceId = createDevice("iPhone 15 Pro", "Apple", State.IN_USE);
        final DevicePatchRequest request = new DevicePatchRequest();
        request.setName("iPhone 16 Pro");

        final ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + deviceId,
                HttpMethod.PATCH,
                new HttpEntity<>(request),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void patchDevice_stateChangeOnInUseDevice_returns200() {
        final UUID deviceId = createDevice("iPhone 15 Pro", "Apple", State.IN_USE);
        final DevicePatchRequest request = new DevicePatchRequest();
        request.setState(State.INACTIVE);

        final ResponseEntity<DeviceResponse> response = restTemplate.exchange(
                baseUrl() + "/" + deviceId,
                HttpMethod.PATCH,
                new HttpEntity<>(request),
                DeviceResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getState()).isEqualTo(State.INACTIVE);
    }

    // ─── DELETE DEVICE ────────────────────────────────────────────────────────────

    @Test
    void deleteDevice_availableDevice_returns204() {
        final UUID deviceId = createDevice("iPhone 15 Pro", "Apple", State.AVAILABLE);

        final ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + deviceId,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(deviceRepository.findByDeviceId(deviceId)).isEmpty();
    }

    @Test
    void deleteDevice_inUseDevice_returns409() {
        final UUID deviceId = createDevice("iPhone 15 Pro", "Apple", State.IN_USE);

        final ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + deviceId,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deleteDevice_nonExistentDevice_returns404() {
        final ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + UUID.randomUUID(),
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ─── HELPER METHODS ────────────────────────────────────────────────────────────

    private UUID createDevice(final String name, final String brand, final State state) {
        final DeviceRequest request = buildRequest(name, brand, state);
        final ResponseEntity<DeviceResponse> response = restTemplate
                .postForEntity(baseUrl(), request, DeviceResponse.class);
        return response.getBody().getDeviceId();
    }
}
