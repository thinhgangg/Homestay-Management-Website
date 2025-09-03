package com.homestay.homestayweb.controller;

import com.homestay.homestayweb.entity.User;
import com.homestay.homestayweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // API lấy thông tin người dùng theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // API lấy tất cả người dùng
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/hosts")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    public List<User> getAllHosts() {
        return userService.getAllHosts();
    }

    // API cập nhật thông tin người dùng
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userService.updateUser(id, updatedUser);
    }

    // API xóa người dùng (dành cho admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // API lấy thông tin hồ sơ của người dùng hiện tại
    @GetMapping("/myprofile")
    @PreAuthorize("hasAnyAuthority('USER_ACCESS', 'HOST_ACCESS', 'ADMIN_ACCESS')")
    public User getMyProfile() {
        return userService.getCurrentUser();
    }

    // API cập nhật thông tin hồ sơ của người dùng hiện tại
    @PutMapping("/myprofile/update")
    @PreAuthorize("hasAnyAuthority('USER_ACCESS', 'HOST_ACCESS', 'ADMIN_ACCESS')")
    public User updateMyProfile(@RequestBody User updatedUser) {
        return userService.updateMyProfile(updatedUser);
    }
}
