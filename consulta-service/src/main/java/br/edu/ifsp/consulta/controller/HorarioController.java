package br.edu.ifsp.consulta.controller;

import br.edu.ifsp.consulta.model.Horario;
import br.edu.ifsp.consulta.service.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/horarios")
@Tag(name = "Horários", description = "Endpoints para gerenciamento de horários de atendimento")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @GetMapping
    @Operation(summary = "Listar todos os horários", description = "Retorna uma lista paginada de todos os horários")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de horários retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public Page<Horario> listarTodas(@Parameter(description = "Parâmetros de paginação") Pageable pageable) {
        return horarioService.listar(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Horario> buscarPorId(@PathVariable Long id) {
        Optional<Horario> horario = horarioService.buscarPorId(id);
        return horario.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Horario> criar(@RequestBody Horario obj) {
        Horario salvo = horarioService.salvar(obj);
        return ResponseEntity.ok(salvo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        horarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profissional/{id}")
    public Page<Horario> listarPorProfissional(@PathVariable Long id, Pageable pageable) {
        return horarioService.listarPorProfissional(id, pageable);
    }

    @GetMapping("/profissionais/{id}/horarios")
    public Page<Horario> listarHorarios(@PathVariable Long id, Pageable pageable) {
        return horarioService.listarHorariosDisponiveis(id, pageable);
    }
} 