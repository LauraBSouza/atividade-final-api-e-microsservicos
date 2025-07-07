package br.edu.ifsp.consulta.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import br.edu.ifsp.consulta.model.Horario;

public interface HorarioRepository extends JpaRepository<Horario, Long>, PagingAndSortingRepository<Horario, Long>{
	Page<Horario> findByProfissionalIdAndDataHoraInicioBetween(Long profissionalId, LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
	Page<Horario> findByDisponivelTrue(Pageable pageable);
	Page<Horario> findByDataHoraInicioBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
	Page<Horario> findByProfissionalId(Long profissionalId, Pageable pageable);
	Page<Horario> findByProfissionalIdAndDisponivelTrue(Long profissionalId, Pageable pageable);
} 