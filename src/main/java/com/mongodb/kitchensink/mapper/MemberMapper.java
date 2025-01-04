package com.mongodb.kitchensink.mapper;

import com.mongodb.kitchensink.document.Member;
import com.mongodb.kitchensink.model.co.UpdateMemberCO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateMember(UpdateMemberCO co, @MappingTarget Member member);
}
