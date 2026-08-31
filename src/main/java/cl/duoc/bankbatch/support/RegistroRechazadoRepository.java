package cl.duoc.bankbatch.support;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroRechazadoRepository extends JpaRepository<RegistroRechazado, Long> {

    long countByProceso(String proceso);
}
