package com.grouplearning.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SubmitDailyChallengeRequest(

        @NotBlank
        @Pattern(regexp = "^[ABCDabcd]$")
        String selectedOption

) {
}