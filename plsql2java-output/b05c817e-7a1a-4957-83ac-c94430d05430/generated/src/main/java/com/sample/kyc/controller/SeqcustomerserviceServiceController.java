package com.sample.kyc.controller;

import com.sample.kyc.service.SeqcustomerserviceService;
import com.sample.kyc.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Seqcustomerservice.
 * Translated from PL/SQL: UNKNOWN.SEQ_CUSTOMER
 */
@RestController
@RequestMapping("/seqcustomerservice")
@Tag(name = "Seqcustomerservice", description = "Operations for Seqcustomerservice")
public class SeqcustomerserviceServiceController {

    private final SeqcustomerserviceService service;

    public SeqcustomerserviceServiceController(SeqcustomerserviceService service) {
        this.service = service;
    }

    @Operation(summary = "Execute execute")
    @PostMapping("/execute")
    public ResponseEntity<SeqcustomerserviceResponse> execute(@Valid @RequestBody SeqcustomerserviceRequest request) {
        return ResponseEntity.ok(service.execute(request));
    }

}
