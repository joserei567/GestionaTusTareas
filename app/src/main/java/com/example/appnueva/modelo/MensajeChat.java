package com.example.appnueva.modelo;

public class MensajeChat {
    // Mensaje sencillo que se guarda dentro del chat de una tarea compartida.
    private String id;
    private String correoAutor;
    private String texto;
    private long fechaEnvio;

    public MensajeChat() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorreoAutor() {
        return correoAutor;
    }

    public void setCorreoAutor(String correoAutor) {
        this.correoAutor = correoAutor;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public long getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(long fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}
