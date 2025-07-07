package br.edu.ifsp.consulta.controller;

import br.edu.ifsp.consulta.model.Consulta;
import br.edu.ifsp.consulta.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/consultas")
@Tag(name = "Consultas", description = "Endpoints para gerenciamento de consultas médicas")
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;

    @GetMapping
    @Operation(summary = "Listar todas as consultas", description = "Retorna uma lista paginada de todas as consultas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de consultas retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public Page<Consulta> listarTodas(@Parameter(description = "Parâmetros de paginação") Pageable pageable) {
        return consultaService.listar(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar consulta por ID", description = "Retorna uma consulta específica pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consulta encontrada"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<Consulta> buscarPorId(@Parameter(description = "ID da consulta") @PathVariable Long id) {
        Optional<Consulta> consulta = consultaService.buscarPorId(id);
        return consulta.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL', 'PACIENTE')")
    @Operation(summary = "Criar nova consulta", description = "Cria uma nova consulta médica. Pacientes podem agendar consultas para si mesmos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consulta criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Consulta> criar(@Parameter(description = "Dados da consulta") @RequestBody Consulta consulta) {
        Consulta salvo = consultaService.salvar(consulta);
        return ResponseEntity.ok(salvo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar consulta", description = "Remove uma consulta pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Consulta deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<Void> deletar(@Parameter(description = "ID da consulta") @PathVariable Long id) {
        consultaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(params = "userId")
    @Operation(summary = "Listar consultas por paciente", description = "Retorna todas as consultas de um paciente específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de consultas do paciente"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<Consulta> listarPorUserId(@Parameter(description = "ID do paciente") @RequestParam("userId") Long userId) {
        return consultaService.buscarPorPacienteId(userId);
    }
} 