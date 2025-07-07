package br.ifsp.consulta_facil_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.consulta_facil_api.dto.AuthenticationDTO;
import br.ifsp.consulta_facil_api.dto.UserInfoDTO;
import br.ifsp.consulta_facil_api.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e geração de tokens JWT")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(
        summary = "Autenticar usuário",
        description = "Autentica um usuário com email e senha, retornando um token JWT válido. " +
                     "O token deve ser incluído no header Authorization para acessar endpoints protegidos. " +
                     "Formato: Bearer {token}"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso",
                    content = @Content(schema = @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    @PostMapping("authenticate")
    public String authenticate(@RequestBody @Valid AuthenticationDTO request) {
        System.out.println("[AUTH] Tentando autenticar: " + request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );
        System.out.println("[AUTH] Autenticação bem-sucedida para: " + request.getEmail());
        return authenticationService.authenticate(authentication);
    }

    @Operation(
        summary = "Validar token e obter informações do usuário",
        description = "Valida um token JWT e retorna as informações do usuário autenticado. " +
                     "Usado pelos microsserviços para validar tokens gerados pelo monolito."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token válido, informações do usuário retornadas"),
        @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    @GetMapping("validate")
    public ResponseEntity<UserInfoDTO> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                // Buscar informações do usuário no banco de dados
                UserInfoDTO userInfo = authenticationService.getUserInfo(authentication.getName());
                return ResponseEntity.ok(userInfo);
            } catch (Exception e) {
                // Log do erro para debug
                System.err.println("Erro ao buscar informações do usuário: " + e.getMessage());
                return ResponseEntity.status(500).build();
            }
        }
        
        return ResponseEntity.status(401).build();
    }
}
