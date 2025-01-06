package com.mongodb.kitchensink.model.co;

import lombok.Builder;

@Builder
public record UpdateMemberCO(String name, String password, String phoneNumber) {
}
