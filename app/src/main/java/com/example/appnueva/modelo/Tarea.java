package com.example.appnueva.modelo;

public class Tarea {
    // Clase principal de datos: representa una tarea guardada en Firebase.
    private String id;
    private String titulo;
    private String descripcion;
    private PrioridadTarea prioridad;
    private EstadoTarea estado;
    private String fechaLimite;
    private String fechaCreacion;
    private String categoriaId;
    private String usuarioId;
    private boolean recordatorioActivo;
    private long fechaRecordatorio;
    private String chatId;
    private String colaboradorCorreo;

    public Tarea() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public PrioridadTarea getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadTarea prioridad) {
        this.prioridad = prioridad;
    }

    public EstadoTarea getEstado() {
        return estado;
    }

    public void setEstado(EstadoTarea estado) {
        this.estado = estado;
    }

    public String getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(String fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(String categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public boolean isRecordatorioActivo() {
        return recordatorioActivo;
    }

    public void setRecordatorioActivo(boolean recordatorioActivo) {
        this.recordatorioActivo = recordatorioActivo;
    }

    public long getFechaRecordatorio() {
        return fechaRecordatorio;
    }

    public void setFechaRecordatorio(long fechaRecordatorio) {
        this.fechaRecordatorio = fechaRecordatorio;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getColaboradorCorreo() {
        return colaboradorCorreo;
    }

    public void setColaboradorCorreo(String colaboradorCorreo) {
        this.colaboradorCorreo = colaboradorCorreo;
    }

    public boolean estaCompartida() {
        // Si tiene chat o correo de colaborador, la app la muestra como compartida.
        return (chatId != null && !chatId.isEmpty())
                || (colaboradorCorreo != null && !colaboradorCorreo.isEmpty());
    }
}
