package br.edu.ifsp.consulta.exception;

public class ConsultaException extends RuntimeException {
    private final CodigosErro codigoErro;

    public ConsultaException(String message, CodigosErro codigoErro) {
        super(message);
        this.codigoErro = codigoErro;
    }

    public CodigosErro getCodigoErro() {
        return codigoErro;
    }
} 