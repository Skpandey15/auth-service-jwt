
package com.example.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private char[] password;
    private String name;
}
