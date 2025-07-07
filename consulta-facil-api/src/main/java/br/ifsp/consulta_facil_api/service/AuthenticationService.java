package br.ifsp.consulta_facil_api.service;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import br.ifsp.consulta_facil_api.dto.UserInfoDTO;
import br.ifsp.consulta_facil_api.model.Usuario;
import br.ifsp.consulta_facil_api.repository.UsuarioRepository;

@Service
public class AuthenticationService {
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    
    public AuthenticationService(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }
    
    public String authenticate(Authentication authentication) {
        String email = authentication.getName();     
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtService.generateToken(usuario);
    }
    
    public UserInfoDTO getUserInfo(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(usuario.getId());
        userInfo.setUsername(usuario.getNome());
        userInfo.setEmail(usuario.getEmail());
        userInfo.setRoles(List.of("ROLE_" + usuario.getRole().name()));
        
        return userInfo;
    }
}