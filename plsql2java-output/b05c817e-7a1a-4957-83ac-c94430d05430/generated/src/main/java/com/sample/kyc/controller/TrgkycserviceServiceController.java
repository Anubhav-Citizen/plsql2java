package com.sample.kyc.controller;

import com.sample.kyc.service.TrgkycserviceService;
import com.sample.kyc.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Trgkycservice.
 * Translated from PL/SQL: UNKNOWN.TRG_KYC
 */
@RestController
@RequestMapping("/trgkycservice")
@Tag(name = "Trgkycservice", description = "Operations for Trgkycservice")
public class TrgkycserviceServiceController {

    private final TrgkycserviceService service;

    public TrgkycserviceServiceController(TrgkycserviceService service) {
        this.service = service;
    }

    @Operation(summary = "Execute execute")
    @PostMapping("/execute")
    public ResponseEntity<TrgkycserviceResponse> execute(@Valid @RequestBody TrgkycserviceRequest request) {
        return ResponseEntity.ok(service.execute(request));
    }

}
