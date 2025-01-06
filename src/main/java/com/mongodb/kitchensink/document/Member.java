package com.mongodb.kitchensink.document;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Member implements Serializable {
    @Id
    String id;

    @Indexed(unique = true)
    String email;

    String name;

    String password;

    List<String> roles;

    String phoneNumber;

}
