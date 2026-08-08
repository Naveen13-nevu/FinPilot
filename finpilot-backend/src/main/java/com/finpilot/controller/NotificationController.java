package com.finpilot.controller;

import com.finpilot.dto.response.ApiResponse;
import com.finpilot.dto.response.NotificationResponse;
import com.finpilot.entity.User;
import com.finpilot.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications for budget alerts, EMI due dates and goal milestones")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all notifications for the current user")
    public ApiResponse<List<NotificationResponse>> getAll(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(notificationService.getAll(currentUser));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get the count of unread notifications")
    public ApiResponse<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(Map.of("unreadCount", notificationService.getUnreadCount(currentUser)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public ApiResponse<NotificationResponse> markAsRead(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        return ApiResponse.success(notificationService.markAsRead(currentUser, id));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal User currentUser) {
        notificationService.markAllAsRead(currentUser);
        return ApiResponse.message("All notifications marked as read");
    }
}