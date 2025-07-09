package com.banquito.core.cuentas.repositorio;

import com.banquito.core.cuentas.modelo.CuentasClientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentasClientesRepositorio extends JpaRepository<CuentasClientes, Integer> {

    Optional<CuentasClientes> findByNumeroCuenta(String numeroCuenta);

    Optional<CuentasClientes> findByIdClienteAndNumeroCuenta(String idCliente, String numeroCuenta);

    Optional<CuentasClientes> findByIdCliente(String idCliente);
}