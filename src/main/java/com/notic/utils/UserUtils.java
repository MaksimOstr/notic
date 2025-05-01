package com.notic.utils;

import com.notic.entity.Role;
import com.notic.entity.User;
import lombok.experimental.UtilityClass;

import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class UserUtils {
    public static Set<String> mapUserRoles(User user) {
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }

}
