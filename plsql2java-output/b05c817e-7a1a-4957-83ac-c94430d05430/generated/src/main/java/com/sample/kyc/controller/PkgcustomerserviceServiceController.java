package com.sample.kyc.controller;

import com.sample.kyc.service.PkgcustomerserviceService;
import com.sample.kyc.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Pkgcustomerservice.
 * Translated from PL/SQL: UNKNOWN.PKG_CUSTOMER
 */
@RestController
@RequestMapping("/pkgcustomerservice")
@Tag(name = "Pkgcustomerservice", description = "Operations for Pkgcustomerservice")
public class PkgcustomerserviceServiceController {

    private final PkgcustomerserviceService service;

    public PkgcustomerserviceServiceController(PkgcustomerserviceService service) {
        this.service = service;
    }

    @Operation(summary = "Execute execute")
    @PostMapping("/execute")
    public ResponseEntity<PkgcustomerserviceResponse> execute(@Valid @RequestBody PkgcustomerserviceRequest request) {
        return ResponseEntity.ok(service.execute(request));
    }

}
