package com.growthmachine.analytics.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErroResposta {
    private String mensagem;
    private int status;
    private String timestamp;
}