package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

@Service
public class StatClient extends BaseClient {

    @Autowired
    public StatClient(@Value("${client.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> create(EndpointHitDto endpointHit) {
        return post("/hit", endpointHit);
    }

    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", UrlEncodeUtils.encode(start),
                "end", UrlEncodeUtils.encode(end),
                "uris", uris,
                "unique", unique
        );
        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }
}