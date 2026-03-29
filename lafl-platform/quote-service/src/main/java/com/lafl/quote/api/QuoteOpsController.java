package com.lafl.quote.api;

import com.lafl.quote.service.QuoteOpsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class QuoteOpsController {

    private final QuoteOpsService quoteOpsService;

    public QuoteOpsController(QuoteOpsService quoteOpsService) {
        this.quoteOpsService = quoteOpsService;
    }

    @PostMapping("/quotes")
    @Operation(summary = "Submit a new quote request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Quote request saved"),
        @ApiResponse(responseCode = "400", description = "Invalid quote request payload")
    })
    public ResponseEntity<?> createQuote(@Valid @RequestBody QuoteCreateRequest request) {
        var record = quoteOpsService.createQuote(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "Quote request received.", "record", record));
    }

    @PostMapping("/contacts")
    @Operation(summary = "Submit a new contact request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Contact request saved"),
        @ApiResponse(responseCode = "400", description = "Invalid contact request payload")
    })
    public ResponseEntity<?> createContact(@Valid @RequestBody ContactCreateRequest request) {
        var record = quoteOpsService.createContact(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "Contact request received.", "record", record));
    }

    @GetMapping("/ops/overview")
    @Operation(summary = "Get operations overview metrics and recent activity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overview returned"),
        @ApiResponse(responseCode = "500", description = "Unable to fetch overview")
    })
    public ResponseEntity<?> opsOverview() {
        return ResponseEntity.ok(quoteOpsService.getOverview());
    }

    @GetMapping("/ops/issues")
    @Operation(summary = "List active shipment issues for operations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active issues returned"),
        @ApiResponse(responseCode = "500", description = "Unable to fetch active issues")
    })
    public ResponseEntity<?> opsIssues() {
        return ResponseEntity.ok(Map.of("shipments", quoteOpsService.getActiveIssues()));
    }
}
