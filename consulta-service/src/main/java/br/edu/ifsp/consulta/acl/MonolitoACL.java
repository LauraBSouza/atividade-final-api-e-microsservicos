package br.edu.ifsp.consulta.acl;

import br.edu.ifsp.consulta.dto.UserInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Component
public class MonolitoACL {

    private final RestTemplate restTemplate;
    
    @Value("${monolito.base-url:http://localhost:8080}")
    private String monolitoBaseUrl;

    public MonolitoACL(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Valida o token JWT com o monolito e retorna informações do usuário
     */
    public Optional<UserInfoDTO> validateTokenAndGetUserInfo(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<?> httpEntity = new HttpEntity<>(headers);

            ResponseEntity<UserInfoDTO> response = restTemplate.exchange(
                monolitoBaseUrl + "/api/auth/validate",
                HttpMethod.GET,
                httpEntity,
                UserInfoDTO.class
            );

            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            // Token inválido ou usuário não encontrado
            return Optional.empty();
        } catch (Exception e) {
            // Erro de comunicação com o monolito
            throw new RuntimeException("Erro ao comunicar com o monolito: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se o usuário tem uma determinada role
     */
    public boolean hasRole(String token, String role) {
        Optional<UserInfoDTO> userInfo = validateTokenAndGetUserInfo(token);
        return userInfo.map(user -> user.getRoles().contains(role)).orElse(false);
    }

    /**
     * Verifica se o usuário tem permissão para acessar um recurso específico
     */
    public boolean hasPermission(String token, String resource, String action) {
        Optional<UserInfoDTO> userInfo = validateTokenAndGetUserInfo(token);
        if (userInfo.isEmpty()) {
            return false;
        }

        UserInfoDTO user = userInfo.get();
        
        // Lógica de permissões baseada em roles
        if (user.getRoles().contains("ROLE_ADMIN")) {
            return true; // Admin tem acesso total
        }
        
        if (user.getRoles().contains("ROLE_PROFISSIONAL")) {
            // Profissionais podem gerenciar consultas
            return "consulta".equals(resource) && ("read".equals(action) || "write".equals(action));
        }
        
        if (user.getRoles().contains("ROLE_PACIENTE")) {
            // Pacientes podem visualizar e criar suas próprias consultas
            return "consulta".equals(resource) && ("read".equals(action) || "write".equals(action));
        }
        
        return false;
    }
} 