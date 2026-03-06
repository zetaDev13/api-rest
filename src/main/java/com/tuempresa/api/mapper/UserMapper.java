package com.tuempresa.api.mapper;

import com.tuempresa.api.model.dto.UserRequest;
import com.tuempresa.api.model.dto.UserResponse;
import com.tuempresa.api.model.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User toEntity(UserRequest request);

    UserResponse toResponse(User entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserRequest request, @MappingTarget User entity);
}
