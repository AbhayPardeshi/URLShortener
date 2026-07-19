package com.springbootprojects.urlshortner.DTO;

import lombok.Data;

@Data
public class CreateUrlResponseDTO {
    String shortURL;
    String longURL;
}
