package com.sample.kyc.controller;

import com.sample.kyc.service.SeqkycserviceService;
import com.sample.kyc.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Seqkycservice.
 * Translated from PL/SQL: UNKNOWN.SEQ_KYC
 */
@RestController
@RequestMapping("/seqkycservice")
@Tag(name = "Seqkycservice", description = "Operations for Seqkycservice")
public class SeqkycserviceServiceController {

    private final SeqkycserviceService service;

    public SeqkycserviceServiceController(SeqkycserviceService service) {
        this.service = service;
    }

    @Operation(summary = "Execute execute")
    @PostMapping("/execute")
    public ResponseEntity<SeqkycserviceResponse> execute(@Valid @RequestBody SeqkycserviceRequest request) {
        return ResponseEntity.ok(service.execute(request));
    }

}
