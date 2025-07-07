package br.edu.ifsp.consulta.service;

import br.edu.ifsp.consulta.model.Horario;
import br.edu.ifsp.consulta.repository.HorarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Page<Horario> listar(Pageable pageable) {
        return horarioRepository.findAll(pageable);
    }

    public Optional<Horario> buscarPorId(Long id) {
        return horarioRepository.findById(id);
    }

    public Horario salvar(Horario horario) {
        return horarioRepository.save(horario);
    }
    
    public Horario atualizar(Long id, Horario horarioAtualizado) {
        Horario horario = horarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Horário não encontrado"));
        horario.setDisponivel(horarioAtualizado.isDisponivel());
        return horarioRepository.save(horario);
    }

    public void deletar(Long id) {
        horarioRepository.deleteById(id);
    }

    public Page<Horario> listarPorProfissional(Long profissionalId, Pageable pageable) {
        return horarioRepository.findByProfissionalId(profissionalId, pageable);
    }

    public Page<Horario> listarHorariosDisponiveis(Long profissionalId, Pageable pageable) {
        return horarioRepository
            .findByProfissionalIdAndDisponivelTrue(profissionalId, pageable);
    }
} 