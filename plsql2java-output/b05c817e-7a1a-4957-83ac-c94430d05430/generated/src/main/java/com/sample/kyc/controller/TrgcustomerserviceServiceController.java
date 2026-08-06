package com.sample.kyc.controller;

import com.sample.kyc.service.TrgcustomerserviceService;
import com.sample.kyc.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Trgcustomerservice.
 * Translated from PL/SQL: UNKNOWN.TRG_CUSTOMER
 */
@RestController
@RequestMapping("/trgcustomerservice")
@Tag(name = "Trgcustomerservice", description = "Operations for Trgcustomerservice")
public class TrgcustomerserviceServiceController {

    private final TrgcustomerserviceService service;

    public TrgcustomerserviceServiceController(TrgcustomerserviceService service) {
        this.service = service;
    }

    @Operation(summary = "Execute execute")
    @PostMapping("/execute")
    public ResponseEntity<TrgcustomerserviceResponse> execute(@Valid @RequestBody TrgcustomerserviceRequest request) {
        return ResponseEntity.ok(service.execute(request));
    }

}
