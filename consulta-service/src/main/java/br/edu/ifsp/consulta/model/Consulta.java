package br.edu.ifsp.consulta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime horario;

    // Agora apenas IDs, não entidades
    private Long pacienteId;
    private Long profissionalId;
    
    @Enumerated(EnumType.STRING)
    private StatusConsulta statusConsulta;
    
    private String observacoes;
} 