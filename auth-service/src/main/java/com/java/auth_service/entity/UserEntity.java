package com.java.auth_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "users")
public class UserEntity extends BaseEntity {

    @Field(name = "password")
    String password;

    @Field(name = "status")
    Boolean status;

    String name;

    String phone;

    String address;

    String email;

    @DBRef
    RoleEntity role;
}
