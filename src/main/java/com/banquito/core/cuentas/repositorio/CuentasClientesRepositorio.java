package com.banquito.core.cuentas.repositorio;

import com.banquito.core.cuentas.modelo.CuentasClientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface CuentasClientesRepositorio extends JpaRepository<CuentasClientes, Integer> {

    // Método que encuentra la primera cuenta por número (maneja duplicados)
    @Query(value = "SELECT * FROM account.cuentas_clientes WHERE numero_cuenta = :numeroCuenta ORDER BY id_cuenta_cliente ASC LIMIT 1", nativeQuery = true)
    Optional<CuentasClientes> findByNumeroCuenta(@Param("numeroCuenta") String numeroCuenta);

    Optional<CuentasClientes> findByIdClienteAndNumeroCuenta(String idCliente, String numeroCuenta);

    // CuentasClientesRepositorio.java
    List<CuentasClientes> findByIdCliente(String idCliente);
}