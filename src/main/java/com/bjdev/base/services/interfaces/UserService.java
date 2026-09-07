package com.bjdev.base.services.interfaces;

import com.bjdev.base.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> listUsers();

    void disableUser(Long userId);

    void enableUser(Long userId);

    void disableSelf();
}
