package com.example.appnueva.modelo;

public class PeticionColaboracion {
    // Invitación que recibe otro usuario para compartir una tarea.
    private String id;
    private String tareaId;
    private String tituloTarea;
    private String propietarioId;
    private String propietarioCorreo;
    private String colaboradorCorreo;
    private String chatId;
    private String estado;

    public PeticionColaboracion() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTareaId() {
        return tareaId;
    }

    public void setTareaId(String tareaId) {
        this.tareaId = tareaId;
    }

    public String getTituloTarea() {
        return tituloTarea;
    }

    public void setTituloTarea(String tituloTarea) {
        this.tituloTarea = tituloTarea;
    }

    public String getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(String propietarioId) {
        this.propietarioId = propietarioId;
    }

    public String getPropietarioCorreo() {
        return propietarioCorreo;
    }

    public void setPropietarioCorreo(String propietarioCorreo) {
        this.propietarioCorreo = propietarioCorreo;
    }

    public String getColaboradorCorreo() {
        return colaboradorCorreo;
    }

    public void setColaboradorCorreo(String colaboradorCorreo) {
        this.colaboradorCorreo = colaboradorCorreo;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
