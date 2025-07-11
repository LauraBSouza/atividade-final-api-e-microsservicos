package br.edu.ifsp.consulta.service;

import br.edu.ifsp.consulta.acl.MonolitoACL;
import br.edu.ifsp.consulta.dto.UserInfoDTO;
import br.edu.ifsp.consulta.model.Consulta;
import br.edu.ifsp.consulta.repository.ConsultaRepository;
import br.edu.ifsp.consulta.repository.HorarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import br.edu.ifsp.consulta.dto.HorarioDTO;
import br.edu.ifsp.consulta.dto.PageResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConsultaService {

    private static final Logger logger = LoggerFactory.getLogger(ConsultaService.class);

    @Autowired
    private ConsultaRepository consultaRepository;
    
    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MonolitoACL monolitoACL;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${monolito.base-url:http://localhost:8080}")
    private String monolitoBaseUrl;

    public Page<Consulta> listar(Pageable pageable) {
        logger.info("Listando consultas com paginação: {}", pageable);
        Page<Consulta> consultas = consultaRepository.findAll(pageable);
        logger.debug("Encontradas {} consultas", consultas.getTotalElements());
        return consultas;
    }
    
    public Optional<Consulta> buscarPorId(Long id) {
        logger.info("Buscando consulta com ID: {}", id);
        Optional<Consulta> consulta = consultaRepository.findById(id);
        if (consulta.isPresent()) {
            logger.debug("Consulta encontrada: {}", id);
        } else {
            logger.warn("Consulta não encontrada: {}", id);
        }
        return consulta;
    }

    public Consulta salvar(Consulta consulta) {
        logger.info("Salvando nova consulta para paciente {} com profissional {} no horário {}", 
                   consulta.getPacienteId(), consulta.getProfissionalId(), consulta.getHorario());
        Long profissionalId = consulta.getProfissionalId();
        LocalDateTime horarioConsulta = consulta.getHorario();
        boolean conflito = consultaRepository.existsByProfissionalIdAndHorario(profissionalId, horarioConsulta);
        if (conflito) {
            logger.warn("Tentativa de agendamento com conflito - Profissional: {}, Horário: {}", profissionalId, horarioConsulta);
            throw new RuntimeException("Já existe uma consulta agendada com este profissional neste horário.");
        }
        
        // Buscar horários disponíveis do monolito
        List<HorarioDTO> horariosDisponiveis = buscarHorariosDisponiveisDoMonolito(profissionalId);
        logger.info("Horários disponíveis encontrados para profissional {}: {}", profissionalId, horariosDisponiveis.size());
        horariosDisponiveis.forEach(h -> 
            logger.info("Horário disponível: ID={}, Início={}, Fim={}, Disponível={}", 
                       h.getId(), h.getDataHoraInicio(), h.getDataHoraFim(), h.isDisponivel()));
        
        boolean horarioDisponivel = horariosDisponiveis
            .stream()
            .anyMatch(h -> {
                boolean dentroDoIntervalo = horarioConsulta.isEqual(h.getDataHoraInicio()) ||
                    (horarioConsulta.isAfter(h.getDataHoraInicio()) && horarioConsulta.isBefore(h.getDataHoraFim()));
                logger.info("Comparando horário {} com intervalo [{}, {}]: {}", 
                           horarioConsulta, h.getDataHoraInicio(), h.getDataHoraFim(), dentroDoIntervalo);
                return dentroDoIntervalo;
            });
        
        if (!horarioDisponivel) {
            logger.warn("Tentativa de agendamento em horário indisponível - Profissional: {}, Horário: {}", profissionalId, horarioConsulta);
            throw new RuntimeException("O horário selecionado não está disponível para este profissional.");
        }
        
        consulta = consultaRepository.save(consulta);
        logger.info("Consulta salva com sucesso. ID: {}", consulta.getId());
        
        // Marcar horário como indisponível no monolito
        marcarHorarioComoIndisponivelNoMonolito(profissionalId, horarioConsulta);
        
        return consulta;
    }
    
    private List<HorarioDTO> buscarHorariosDisponiveisDoMonolito(Long profissionalId) {
        try {
            String url = monolitoBaseUrl + "/horarios/profissionais/" + profissionalId + "/horarios?size=100";
            ResponseEntity<PageResponseDTO<HorarioDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), 
                new org.springframework.core.ParameterizedTypeReference<PageResponseDTO<HorarioDTO>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Resposta do monolito: {} horários encontrados", response.getBody().getContent().size());
                return response.getBody().getContent();
            } else {
                logger.warn("Erro ao buscar horários do monolito. Status: {}", response.getStatusCode());
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar horários disponíveis do monolito: {}", e.getMessage());
            return List.of();
        }
    }
    
    private void marcarHorarioComoIndisponivelNoMonolito(Long profissionalId, LocalDateTime horarioConsulta) {
        try {
            // Buscar o horário específico e marcá-lo como indisponível
            List<HorarioDTO> horarios = buscarHorariosDisponiveisDoMonolito(profissionalId);
            horarios.stream()
                .filter(h -> horarioConsulta.isEqual(h.getDataHoraInicio()) ||
                    (horarioConsulta.isAfter(h.getDataHoraInicio()) && horarioConsulta.isBefore(h.getDataHoraFim())))
                .findFirst()
                .ifPresent(horario -> {
                    horario.setDisponivel(false);
                    String url = monolitoBaseUrl + "/horarios/" + horario.getId();
                    restTemplate.put(url, horario);
                    logger.debug("Horário marcado como indisponível no monolito. ID: {}", horario.getId());
                });
        } catch (Exception e) {
            logger.error("Erro ao marcar horário como indisponível no monolito: {}", e.getMessage());
        }
    }

    public void deletar(Long id) {
        logger.info("Tentando deletar consulta com ID: {}", id);
        Optional<Consulta> optionalConsulta = consultaRepository.findById(id);
        if (optionalConsulta.isEmpty()) {
            logger.warn("Tentativa de deletar consulta inexistente. ID: {}", id);
            throw new RuntimeException("Consulta não encontrada");
        }
        Consulta consulta = optionalConsulta.get();
        if (consulta.getHorario().isBefore(LocalDateTime.now().plusHours(24))) {
            logger.warn("Tentativa de cancelamento sem antecedência de 24h. ID: {}, Horário: {}", id, consulta.getHorario());
            throw new RuntimeException("Cancelamentos só são permitidos com pelo menos 24h de antecedência.");
        }
        consultaRepository.deleteById(id);
        logger.info("Consulta deletada com sucesso. ID: {}", id);
    }

    public List<Consulta> buscarPorPacienteId(Long pacienteId) {
        return consultaRepository.findByPacienteId(pacienteId, Pageable.unpaged()).getContent();
    }

    /**
     * Busca consultas por paciente com validação de permissão via ACL
     */
    public List<Consulta> buscarPorPacienteIdComPermissao(Long pacienteId, String token) {
        logger.info("Buscando consultas do paciente {} com validação de permissão", pacienteId);
        
        // Validar token e obter informações do usuário via ACL
        Optional<UserInfoDTO> userInfo = monolitoACL.validateTokenAndGetUserInfo(token);
        if (userInfo.isEmpty()) {
            logger.warn("Token inválido ao buscar consultas do paciente {}", pacienteId);
            throw new RuntimeException("Token inválido");
        }
        
        UserInfoDTO user = userInfo.get();
        
        // Verificar permissões baseadas em roles
        if (user.getRoles().contains("ROLE_ADMIN")) {
            // Admin pode ver todas as consultas
            logger.debug("Admin acessando consultas do paciente {}", pacienteId);
            return buscarPorPacienteId(pacienteId);
        }
        
        if (user.getRoles().contains("ROLE_PROFISSIONAL")) {
            // Profissional pode ver consultas onde ele é o profissional
            logger.debug("Profissional acessando consultas do paciente {}", pacienteId);
            return consultaRepository.findByProfissionalIdAndPacienteId(user.getId(), pacienteId, Pageable.unpaged()).getContent();
        }
        
        if (user.getRoles().contains("ROLE_PACIENTE")) {
            // Paciente só pode ver suas próprias consultas
            if (!user.getId().equals(pacienteId)) {
                logger.warn("Paciente {} tentando acessar consultas de outro paciente {}", user.getId(), pacienteId);
                throw new RuntimeException("Acesso negado: você só pode visualizar suas próprias consultas");
            }
            logger.debug("Paciente acessando suas próprias consultas");
            return buscarPorPacienteId(pacienteId);
        }
        
        logger.warn("Usuário sem permissão tentando acessar consultas do paciente {}", pacienteId);
        throw new RuntimeException("Acesso negado: permissão insuficiente");
    }
} 