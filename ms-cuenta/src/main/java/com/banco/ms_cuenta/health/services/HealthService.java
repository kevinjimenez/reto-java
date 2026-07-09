package com.banco.ms_cuenta.health.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class HealthService {

    public ResponseEntity<Object> statusHealth() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
