package com.exemple.apipagamento.portalchurras.application.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO padronizado para respostas de erro da API.
 * Fornece informações consistentes sobre erros para os clientes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String message;
    private String code;
    private LocalDateTime timestamp;
    private List<String> details;
    private String path;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message) {
        this();
        this.message = message;
    }

    public ErrorResponse(String message, String code) {
        this(message);
        this.code = code;
    }

    public ErrorResponse(String message, String code, List<String> details) {
        this(message, code);
        this.details = details;
    }

    // Métodos estáticos para criar respostas comuns
    public static ErrorResponse badRequest(String message) {
        return new ErrorResponse(message, "BAD_REQUEST");
    }

    public static ErrorResponse unauthorized(String message) {
        return new ErrorResponse(message, "UNAUTHORIZED");
    }

    public static ErrorResponse forbidden(String message) {
        return new ErrorResponse(message, "FORBIDDEN");
    }

    public static ErrorResponse notFound(String message) {
        return new ErrorResponse(message, "NOT_FOUND");
    }

    public static ErrorResponse internalError(String message) {
        return new ErrorResponse(message, "INTERNAL_SERVER_ERROR");
    }

    public static ErrorResponse conflict(String message) {
        return new ErrorResponse(message, "CONFLICT");
    }

    // Getters e Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
