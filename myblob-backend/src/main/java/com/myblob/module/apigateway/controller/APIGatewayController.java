package com.myblob.module.apigateway.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.apigateway.service.APIGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
public class APIGatewayController {

    private final APIGatewayService apiGatewayService;

    @GetMapping("/services/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getServices() {
        return ResponseEntity.ok(ApiResponse.success(apiGatewayService.getServices()));
    }

    @PostMapping("/services/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createService(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(apiGatewayService.createService(body)));
    }

    @GetMapping("/endpoints/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEndpoints(
            @RequestParam Long service_id) {
        return ResponseEntity.ok(ApiResponse.success(apiGatewayService.getEndpoints(service_id)));
    }

    @GetMapping("/api-keys/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyAPIKeys() {
        return ResponseEntity.ok(ApiResponse.success(apiGatewayService.getMyAPIKeys()));
    }

    @PostMapping("/api-keys/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAPIKey(
            @RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        apiGatewayService.createAPIKey(body.get("name"), body.get("description"))));
    }

    @DeleteMapping("/api-keys/{id}/")
    public ResponseEntity<ApiResponse<Void>> revokeAPIKey(@PathVariable Long id) {
        apiGatewayService.revokeAPIKey(id);
        return ResponseEntity.ok(ApiResponse.success("API Key 已撤销", null));
    }

    @GetMapping("/call-logs/")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getMyCallLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(apiGatewayService.getMyCallLogs(page, size)));
    }
}
