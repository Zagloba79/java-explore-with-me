package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {
    @NotBlank
    @Size(min = 6, max = 254)
    @Email
    private String email;
    @NotBlank
    @Size(min = 2, max = 250)
    private String name;
}
