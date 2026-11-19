package br.com.fiap.alertaencosta.web.controller;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Monta o header Location (201 Created) a partir da URI da requisicao atual. */
final class Uris {

    private Uris() {
    }

    static URI filho(Object id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
