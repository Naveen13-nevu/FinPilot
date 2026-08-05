package com.finpilot.mapper;

import com.finpilot.dto.response.UserResponse;
import com.finpilot.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);
}
