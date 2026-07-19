package com.springbootprojects.urlshortner.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateUrlRequestDTO {

    String longURL;
    String shortURL;
    boolean customAlias;
    UUID idempotencyKey;
}
