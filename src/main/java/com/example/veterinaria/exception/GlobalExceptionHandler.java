package com.example.veterinaria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String errorTipoDato(Model model) {
        model.addAttribute("codigo", "400");
        model.addAttribute("mensaje", "Solicitud inválida. El identificador ingresado no tiene un formato válido.");
        return "error/error-generico";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String errorEstado(ResponseStatusException ex, HttpServletResponse response, Model model) {
        response.setStatus(ex.getStatusCode().value());
        model.addAttribute("codigo", String.valueOf(ex.getStatusCode().value()));
        model.addAttribute("mensaje", "No tienes permiso para acceder a este recurso o no existe.");
        return "error/error-generico";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorGeneral(Model model) {
        model.addAttribute("codigo", "Error");
        model.addAttribute("mensaje", "Ocurrió un problema al procesar tu solicitud. Inténtalo nuevamente.");
        return "error/error-generico";
    }
}