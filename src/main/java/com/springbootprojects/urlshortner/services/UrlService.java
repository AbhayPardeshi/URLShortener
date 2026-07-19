package com.springbootprojects.urlshortner.services;

import com.springbootprojects.urlshortner.DTO.CreateUrlRequestDTO;
import com.springbootprojects.urlshortner.DTO.CreateUrlResponseDTO;
import com.springbootprojects.urlshortner.entities.IdempotencyKey;
import com.springbootprojects.urlshortner.entities.Url;
import com.springbootprojects.urlshortner.repository.IdempotencyRepository;
import com.springbootprojects.urlshortner.repository.UrlRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.springbootprojects.urlshortner.utility.Base62Encoder.base62Encoder;

@Service
public class UrlService {
    private final IdempotencyRepository idempotencyRepository;
    private final UrlRepository urlRepository;
    private final ObjectMapper objectMapper;

    public UrlService(IdempotencyRepository idempotencyRepository, UrlRepository urlRepository, ObjectMapper objectMapper){
        this.idempotencyRepository = idempotencyRepository;
        this.urlRepository = urlRepository;
        this.objectMapper = objectMapper;
    }

    public CreateUrlResponseDTO createShortUrl(CreateUrlRequestDTO request, UUID idempotencyKey) throws Exception {
        // 1. check if the idempotency key already present
        Optional<IdempotencyKey> existingId = idempotencyRepository.findById(idempotencyKey);

        if(existingId.isPresent()){
            CreateUrlResponseDTO cachedResponse = objectMapper.readValue(
                    existingId.get().getResponseBody(),
                    CreateUrlResponseDTO.class
            );
            return cachedResponse;
        }

        // 2. check if custom alias already present, if yes throw 409
        if(request.isCustomAlias()){
            Optional<Url> customAlias = urlRepository.findByShortURL(request.getShortURL());
            if(customAlias.isPresent()){
                throw new Exception("Custom alias already taken");
            }
        }


        //3. create a new shortURL
        Url url = new Url();

        url.setCreatedAt(Instant.now());
        url.setLongURL(request.getLongURL());
        url.setClickCount(0);

        if(request.isCustomAlias()){
            url.setShortURL(request.getShortURL());
            url.setCustomAlias(true);
        }else{
            // for now before we add the logic of creating short url
            url.setShortURL(null);
            url.setCustomAlias(false);
            Long id1 = urlRepository.save(url).getId();
            // base encode logic here to create a new short url
            String shorturl = base62Encoder(id1);
            url.setShortURL(shorturl);
        }

        urlRepository.save(url);

        CreateUrlResponseDTO responseDTO = new CreateUrlResponseDTO();
        responseDTO.setLongURL(url.getLongURL());
        responseDTO.setShortURL(url.getShortURL());

        String responseBody = objectMapper.writeValueAsString(responseDTO);

        IdempotencyKey idempotencyRecord = new IdempotencyKey();
        idempotencyRecord.setIdempotencyId(idempotencyKey);
        idempotencyRecord.setResponseBody(responseBody);
        idempotencyRecord.setCreatedAt(Instant.now());

        idempotencyRepository.save(idempotencyRecord);

        return responseDTO;

    }


}
