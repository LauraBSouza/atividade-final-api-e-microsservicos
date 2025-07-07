package br.edu.ifsp.consulta.dto;

import java.time.LocalDateTime;

public class HorarioDTO {
    private Long id;
    private LocalDateTime dataHora;
    private boolean disponivel;
    // Adicione outros campos relevantes

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
} 