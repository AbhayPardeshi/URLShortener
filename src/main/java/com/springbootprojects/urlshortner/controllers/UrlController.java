package com.springbootprojects.urlshortner.controllers;

import com.springbootprojects.urlshortner.DTO.CreateUrlRequestDTO;
import com.springbootprojects.urlshortner.DTO.CreateUrlResponseDTO;
import com.springbootprojects.urlshortner.services.UrlService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UrlController {

    private final UrlService urlService;

    public UrlController (UrlService urlService){
        this.urlService = urlService;
    }

    @PostMapping("/urls")
    public CreateUrlResponseDTO createShortURL(@RequestBody CreateUrlRequestDTO requestDTO) throws Exception {
        return urlService.createShortUrl(requestDTO,requestDTO.getIdempotencyKey());
    }

    @GetMapping("/{shortCode}")
    public String getLongURL(@PathVariable String shortCode) throws Exception{
        return urlService.getLongURL(shortCode);
    }
}
