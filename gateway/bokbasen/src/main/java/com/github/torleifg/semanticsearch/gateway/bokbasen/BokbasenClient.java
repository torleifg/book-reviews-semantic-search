package com.github.torleifg.semanticsearch.gateway.bokbasen;

import org.editeur.ns.onix._3_0.reference.ONIXMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

class BokbasenClient {
    private final RestClient restClient;

    BokbasenClient(RestClient restClient) {
        this.restClient = restClient;
    }

    ResponseEntity<ONIXMessage> get(String uri) {
        return restClient
                .get()
                .uri(uri)
                .retrieve()
                .toEntity(ONIXMessage.class);
    }
}