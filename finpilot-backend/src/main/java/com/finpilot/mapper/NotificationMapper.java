package com.finpilot.mapper;

import com.finpilot.dto.response.NotificationResponse;
import com.finpilot.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}