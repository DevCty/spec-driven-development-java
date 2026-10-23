package com.stavaray.auth.application.controller;

import com.stavaray.auth.application.dto.UserResponse;
import com.stavaray.auth.application.generated.api.UsersApi;
import com.stavaray.auth.domain.model.User;
import com.stavaray.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    private final UserRepository userRepository;

    @Override
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setName(user.getName());
                    response.setEmail(user.getEmail());
                    return response;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }
}
