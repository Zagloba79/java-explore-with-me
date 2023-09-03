package ru.practicum;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.List;
import java.util.Map;

public class StatClient extends BaseClient {

    public void setUpStatClient(String serverUrl) {
        super.rest = new RestTemplateBuilder().uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
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

    private String encodeValue(String value) {
        return UrlEncodeUtils.encode(value);
    }
}