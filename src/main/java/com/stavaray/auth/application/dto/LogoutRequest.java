package com.stavaray.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LogoutRequest {
    @JsonProperty("refreshToken")
    private String refreshToken;
}
