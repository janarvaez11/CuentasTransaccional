package com.banquito.core.cuentas.repositorio;

import com.banquito.core.cuentas.enums.EstadoGeneralCuentasEnum;
import com.banquito.core.cuentas.modelo.Cuentas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentasRepositorio extends JpaRepository<Cuentas, Integer> {

    // Métodos de validación de unicidad
    boolean existsByCodigoCuenta(String codigoCuenta);
    boolean existsByNombre(String nombre);

    // Métodos de consulta
    Optional<Cuentas> findByCodigoCuenta(String codigoCuenta);
    
    List<Cuentas> findByTipoCuentaId(String tipoCuentaId);
    List<Cuentas> findByTasaInteresId(String tasaInteresId);
    List<Cuentas> findByEstado(EstadoGeneralCuentasEnum estado);
}
