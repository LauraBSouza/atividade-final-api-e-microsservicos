package br.edu.ifsp.consulta.security;

import br.edu.ifsp.consulta.acl.MonolitoACL;
import br.edu.ifsp.consulta.dto.UserInfoDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ACLAuthenticationFilter extends OncePerRequestFilter {

    private final MonolitoACL monolitoACL;

    public ACLAuthenticationFilter(MonolitoACL monolitoACL) {
        this.monolitoACL = monolitoACL;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                Optional<UserInfoDTO> userInfo = monolitoACL.validateTokenAndGetUserInfo(token);
                
                if (userInfo.isPresent()) {
                    UserInfoDTO user = userInfo.get();
                    
                    // Criar authorities baseadas nas roles do usuário
                    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                    
                    // Criar autenticação
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            user.getUsername(), 
                            null, 
                            authorities
                        );
                    
                    // Definir autenticação no contexto de segurança
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Log do erro mas não interrompe o fluxo
                logger.error("Erro ao validar token com monolito: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
} 