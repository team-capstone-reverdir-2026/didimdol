package com.didimdol.global.controller;

import com.didimdol.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 업타임 모니터/배포 플랫폼 헬스체크용 (인증 불필요) */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<Void> health() {
        return ApiResponse.success("OK");
    }
}
