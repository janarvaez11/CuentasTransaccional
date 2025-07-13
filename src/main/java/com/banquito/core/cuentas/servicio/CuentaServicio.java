package com.banquito.core.cuentas.servicio;

import com.banquito.core.cuentas.cliente.TipoCuentaCliente;

import com.banquito.core.cuentas.dto.CuentaRespuestaDTO;
import com.banquito.core.cuentas.dto.CuentaSolicitudDTO;
import com.banquito.core.cuentas.cliente.TasaInteresCliente;
import com.banquito.core.cuentas.dto.TipoCuentaDTO;
import com.banquito.core.cuentas.dto.TasaInteresRespuestaDTO_IdOnly;
import com.banquito.core.cuentas.excepcion.ActualizarEntidadExcepcion;
import com.banquito.core.cuentas.excepcion.CrearEntidadExcepcion;
import com.banquito.core.cuentas.excepcion.EliminarEntidadExcepcion;
import com.banquito.core.cuentas.excepcion.EntidadNoEncontradaExcepcion;
import com.banquito.core.cuentas.mapper.CuentaMapper;
import com.banquito.core.cuentas.modelo.Cuentas;
import com.banquito.core.cuentas.repositorio.CuentasRepositorio;


import feign.FeignException;

import com.banquito.core.cuentas.enums.EstadoGeneralCuentasEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
public class CuentaServicio {

    private final CuentasRepositorio cuentasRepo;
    private final TipoCuentaCliente tipoCuentaCliente;
    private final TasaInteresCliente tasaInteresCliente;

    public CuentaServicio(
            CuentasRepositorio cuentasRepo,
            TipoCuentaCliente tipoCuentaCliente,
            TasaInteresCliente tasaInteresCliente) {
        this.cuentasRepo = cuentasRepo;
        this.tipoCuentaCliente = tipoCuentaCliente;
        this.tasaInteresCliente = tasaInteresCliente;
    }

    @Transactional
    public CuentaRespuestaDTO crear(CuentaSolicitudDTO dto) {

        // 1) Validar unicidad de código y nombre
        if (cuentasRepo.existsByCodigoCuenta(dto.getCodigoCuenta())) {
            log.warn("Intento de crear cuenta con código duplicado={}", dto.getCodigoCuenta());
            throw new CrearEntidadExcepcion("Cuentas",
                    "Ya existe una cuenta con código '" + dto.getCodigoCuenta() + "'");
        }
        if (cuentasRepo.existsByNombre(dto.getNombre())) {
            log.warn("Intento de crear cuenta con nombre duplicado={}", dto.getNombre());
            throw new CrearEntidadExcepcion("Cuentas",
                    "Ya existe una cuenta con nombre '" + dto.getNombre() + "'");
        }

        Cuentas entity = CuentaMapper.toEntity(dto);
        Instant ahora = Instant.now();
        entity.setFechaCreacion(ahora);
        entity.setFechaModificacion(ahora);
        entity.setVersion(1L);
        // estado por defecto = ACTIVO vía entidad

        Cuentas guardada;
        try {
            guardada = cuentasRepo.save(entity);
            log.info("Cuenta creada con ID={}", guardada.getId());
        } catch (Exception e) {
            log.error("Error al crear Cuenta: {}", e.getMessage(), e);
            throw new CrearEntidadExcepcion("Cuentas",
                    "Error al guardar nueva cuenta: " + e.getMessage());
        }

        // 3) Validar existencia remota de Tipo de Cuenta
        TipoCuentaDTO tipoDto;
        try {
            tipoDto = tipoCuentaCliente.obtenerPorId(dto.getIdTipoCuenta());
        } catch (FeignException.NotFound nf) {
            log.error("TipoCuenta no existe ID={}", dto.getIdTipoCuenta());
            throw new CrearEntidadExcepcion("Cuentas",
                    "Cuenta creada pero fallo al recuperar TipoCuenta: ID no encontrado");
        } catch (Exception ex) {
            log.error("Error al obtener TipoCuenta ID={}: {}", dto.getIdTipoCuenta(), ex.getMessage());
            throw new CrearEntidadExcepcion("Cuentas",
                    "Cuenta creada pero fallo al recuperar TipoCuenta: " + ex.getMessage());
        }

        // 4) Validar existencia remota de Tasa de Interés
        TasaInteresRespuestaDTO_IdOnly tasaDto;
        try {
            tasaDto = tasaInteresCliente.obtenerPorId(dto.getIdTasaInteres());
        } catch (FeignException.NotFound nf) {
            log.error("TasaInteres no existe ID={}", dto.getIdTasaInteres());
            throw new CrearEntidadExcepcion("Cuentas",
                    "Cuenta creada pero fallo al recuperar TasaInteres: ID no encontrado");
        } catch (Exception ex) {
            log.error("Error al obtener TasaInteres ID={}: {}", dto.getIdTasaInteres(), ex.getMessage());
            throw new CrearEntidadExcepcion("Cuentas",
                    "Cuenta creada pero fallo al recuperar TasaInteres: " + ex.getMessage());
        }

        return CuentaMapper.toDto(guardada, tipoDto, tasaDto);
    }


    @Transactional(readOnly = true)
    public CuentaRespuestaDTO obtener(Integer id) {
        Cuentas e = cuentasRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("No se encontró Cuenta ID={}", id);
                    return new EntidadNoEncontradaExcepcion("Cuentas",
                            "No existe cuenta con ID=" + id);
                });

        TipoCuentaDTO tipoDto;
        try {
            tipoDto = tipoCuentaCliente.obtenerPorId(e.getTipoCuentaId());
        } catch (Exception ex) {
            log.error("Error cargando TipoCuenta para ID={}: {}", id, ex.getMessage());
            throw new EntidadNoEncontradaExcepcion("Cuentas",
                    "Cuenta encontrada pero fallo al cargar TipoCuenta: " + ex.getMessage());
        }

        TasaInteresRespuestaDTO_IdOnly tasaDto;
        try {
            tasaDto = tasaInteresCliente.obtenerPorId(e.getTasaInteresId());
        } catch (Exception ex) {
            log.error("Error cargando TasaInteres para ID={}: {}", id, ex.getMessage());
            throw new EntidadNoEncontradaExcepcion("Cuentas",
                    "Cuenta encontrada pero fallo al cargar TasaInteres: " + ex.getMessage());
        }

        return CuentaMapper.toDto(e, tipoDto, tasaDto);
    }

    @Transactional
    public CuentaRespuestaDTO actualizar(Integer id, CuentaSolicitudDTO dto) {
        Cuentas e = cuentasRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de actualizar Cuenta inexistente ID={}", id);
                    return new EntidadNoEncontradaExcepcion("Cuentas",
                            "No existe cuenta con ID=" + id);
                });

        // Aplicar cambios y versionar
        e.setTipoCuentaId(dto.getIdTipoCuenta());
        e.setTasaInteresId(dto.getIdTasaInteres());
        e.setCodigoCuenta(dto.getCodigoCuenta());
        e.setNombre(dto.getNombre());
        e.setDescripcion(dto.getDescripcion());
        e.setFechaModificacion(Instant.now());
        e.setVersion(e.getVersion() + 1);

        Cuentas updated;
        try {
            updated = cuentasRepo.save(e);
            log.info("Cuenta actualizada ID={}", id);
        } catch (Exception ex) {
            log.error("Error actualizando Cuenta ID={}: {}", id, ex.getMessage(), ex);
            throw new ActualizarEntidadExcepcion("Cuentas",
                    "Error al actualizar cuenta: " + ex.getMessage());
        }

        // enriquecer datos remotos
        TipoCuentaDTO tipoDto;
        try {
            tipoDto = tipoCuentaCliente.obtenerPorId(updated.getTipoCuentaId());
        } catch (Exception ex) {
            log.error("Error cargando TipoCuenta tras actualizar ID={}: {}", id, ex.getMessage());
            throw new ActualizarEntidadExcepcion("Cuentas",
                    "Cuenta actualizada pero fallo al cargar TipoCuenta: " + ex.getMessage());
        }

        TasaInteresRespuestaDTO_IdOnly tasaDto;
        try {
            tasaDto = tasaInteresCliente.obtenerPorId(updated.getTasaInteresId());
        } catch (Exception ex) {
            log.error("Error cargando TasaInteres tras actualizar ID={}: {}", id, ex.getMessage());
            throw new ActualizarEntidadExcepcion("Cuentas",
                    "Cuenta actualizada pero fallo al cargar TasaInteres: " + ex.getMessage());
        }

        return CuentaMapper.toDto(updated, tipoDto, tasaDto);
    }

    @Transactional
    public void eliminar(Integer id) {
        Optional<Cuentas> opt = cuentasRepo.findById(id);
        if (opt.isEmpty()) {
            log.warn("Intento de eliminar lógicamente cuenta inexistente ID={}", id);
            throw new EntidadNoEncontradaExcepcion("Cuentas",
                    "No existe cuenta con ID=" + id);
        }

        Cuentas e = opt.get();
        e.setEstado(EstadoGeneralCuentasEnum.INACTIVO);
        e.setFechaModificacion(Instant.now());
        e.setVersion(e.getVersion() + 1);

        try {
            cuentasRepo.save(e);
            log.info("Cuenta marcada como INACTIVO ID={}", id);
        } catch (Exception ex) {
            log.error("Error al eliminar lógicamente Cuenta ID={}: {}", id, ex.getMessage(), ex);
            throw new EliminarEntidadExcepcion("Cuentas",
                    "Error al marcar como inactiva: " + ex.getMessage());
        }
    }
}
