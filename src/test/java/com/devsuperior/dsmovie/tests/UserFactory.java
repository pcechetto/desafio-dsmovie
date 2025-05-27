package com.devsuperior.dsmovie.tests;

import com.devsuperior.dsmovie.entities.RoleEntity;
import com.devsuperior.dsmovie.entities.UserEntity;

public class UserFactory {

    public static UserEntity createUserEntity() {
        UserEntity user = new UserEntity(2L, "Maria", "maria@gmail.com", "123");
        user.addRole(new RoleEntity(1L, "ROLE_CLIENT"));
        return user;
    }
}
