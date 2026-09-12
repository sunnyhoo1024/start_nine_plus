package com.snp.app.controller;

import com.snp.app.security.AppAuthContext;
import com.snp.common.api.ApiResponse;
import com.snp.dto.HomeAggregate;
import com.snp.service.CmsService;
import com.snp.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final CmsService cmsService;
    private final PointsService pointsService;

    @GetMapping
    public ApiResponse<HomeAggregate> home() {
        Long userId = AppAuthContext.currentUserId();
        Long balance = null;
        if (userId != null) {
            balance = pointsService.getBalance(userId);
        }
        return ApiResponse.ok(HomeAggregate.builder()
                .banners(cmsService.listPublishedBanners())
                .intro(cmsService.getPublishedBlockByCode("home_intro"))
                .balance(balance)
                .build());
    }
}
