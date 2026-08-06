package com.example.migrated.controller;

import com.example.migrated.service.EmppkgserviceService;
import com.example.migrated.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Emppkgservice.
 * Translated from PL/SQL: UNKNOWN.EMP_PKG
 */
@RestController
@RequestMapping("/emppkgservice")
@Tag(name = "Emppkgservice", description = "Operations for Emppkgservice")
public class EmppkgserviceServiceController {

    private final EmppkgserviceService service;

    public EmppkgserviceServiceController(EmppkgserviceService service) {
        this.service = service;
    }

    @Operation(summary = "Execute execute")
    @PostMapping("/execute")
    public ResponseEntity<EmppkgserviceResponse> execute(@Valid @RequestBody EmppkgserviceRequest request) {
        return ResponseEntity.ok(service.execute(request));
    }

}
