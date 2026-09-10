package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> listUsers();

    void disableUser(Long userId);

    void enableUser(Long userId);

    void disableSelf();
}
