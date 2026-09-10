package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReviewVoteRequest(@NotNull Boolean helpful) {
}
