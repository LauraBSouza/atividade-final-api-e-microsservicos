package br.edu.ifsp.consulta.dto;

import java.time.LocalDateTime;

public class ConsultaDTO {
    private Long id;
    private Long pacienteId;
    private Long profissionalId;
    private LocalDateTime dataHora;
    // Adicione outros campos relevantes

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    public Long getProfissionalId() { return profissionalId; }
    public void setProfissionalId(Long profissionalId) { this.profissionalId = profissionalId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
} 