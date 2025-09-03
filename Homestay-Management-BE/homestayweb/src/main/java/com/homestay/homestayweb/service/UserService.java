package com.homestay.homestayweb.service;

import com.homestay.homestayweb.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, User updatedUser);
    void deleteUser(Long id);
    User updateMyProfile(User updatedUser);
    User getCurrentUser();

    List<User> getAllHosts();
}
