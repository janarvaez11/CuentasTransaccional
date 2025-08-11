package com.banquito.core.cuentas.producer;

import com.banquito.core.cuentas.dto.TransaccionesSolicitudDTO;
import com.banquito.core.cuentas.enums.TipoTransaccionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransaccionesProducer {

    private final JmsTemplate jmsTemplate;
    private final String depositoQueue;
    private final String retiroQueue;

    public TransaccionesProducer(
            JmsTemplate jmsTemplate,
            @Value("${colas.transacciones.deposito:transacciones.deposito}") String depositoQueue,
            @Value("${colas.transacciones.retiro:transacciones.retiro}") String retiroQueue) {
        this.jmsTemplate = jmsTemplate;
        this.depositoQueue = depositoQueue;
        this.retiroQueue = retiroQueue;
    }

    public void enviarDeposito(TransaccionesSolicitudDTO dto) {
        try {
            log.info("Enviando DEPÓSITO a cola: {} para cuenta: {}", depositoQueue, dto.getNumeroCuentaOrigen());
            jmsTemplate.convertAndSend(depositoQueue, dto);
            log.info("Depósito enviado exitosamente a cola");
        } catch (Exception e) {
            log.error("Error enviando depósito a cola: {}", e.getMessage(), e);
            throw new RuntimeException("Error enviando depósito a cola", e);
        }
    }

    public void enviarRetiro(TransaccionesSolicitudDTO dto) {
        try {
            log.info("Enviando RETIRO a cola: {} para cuenta: {}", retiroQueue, dto.getNumeroCuentaOrigen());
            jmsTemplate.convertAndSend(retiroQueue, dto);
            log.info("Retiro enviado exitosamente a cola");
        } catch (Exception e) {
            log.error("Error enviando retiro a cola: {}", e.getMessage(), e);
            throw new RuntimeException("Error enviando retiro a cola", e);
        }
    }

    public void enviarTransferencia(TransaccionesSolicitudDTO dto) {
        try {
            log.info("Procesando TRANSFERENCIA: {} -> {} por ${}",
                    dto.getNumeroCuentaOrigen(), dto.getNumeroCuentaDestino(), dto.getMonto());

            // 1. Crear DTO para RETIRO de cuenta origen
            TransaccionesSolicitudDTO retiroDto = TransaccionesSolicitudDTO.builder()
                    .numeroCuentaOrigen(dto.getNumeroCuentaOrigen())
                    .tipoTransaccion(TipoTransaccionEnum.RETIRO)
                    .monto(dto.getMonto())
                    .descripcion("TRANSFERENCIA A " + dto.getNumeroCuentaDestino() + " - " +
                            (dto.getDescripcion() != null ? dto.getDescripcion() : ""))
                    .build();

            // 2. Crear DTO para DEPÓSITO en cuenta destino
            TransaccionesSolicitudDTO depositoDto = TransaccionesSolicitudDTO.builder()
                    .numeroCuentaOrigen(dto.getNumeroCuentaDestino())
                    .tipoTransaccion(TipoTransaccionEnum.DEPOSITO)
                    .monto(dto.getMonto())
                    .descripcion("TRANSFERENCIA DE " + dto.getNumeroCuentaOrigen() + " - " +
                            (dto.getDescripcion() != null ? dto.getDescripcion() : ""))
                    .build();

            // 3. Enviar RETIRO a cola
            log.info("Enviando RETIRO de transferencia a cola: {} para cuenta {}",
                    retiroQueue, dto.getNumeroCuentaOrigen());
            jmsTemplate.convertAndSend(retiroQueue, retiroDto);

            // 4. Enviar DEPÓSITO a cola
            log.info("Enviando DEPÓSITO de transferencia a cola: {} para cuenta {}",
                    depositoQueue, dto.getNumeroCuentaDestino());
            jmsTemplate.convertAndSend(depositoQueue, depositoDto);

            log.info("Transferencia procesada exitosamente: 2 operaciones enviadas a colas");

        } catch (Exception e) {
            log.error("Error procesando transferencia: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando transferencia", e);
        }
    }

    // Método unificado (mantener por compatibilidad)
    public void enviarTransaccion(TransaccionesSolicitudDTO dto) {
        switch (dto.getTipoTransaccion()) {
            case DEPOSITO:
                enviarDeposito(dto);
                break;
            case RETIRO:
                enviarRetiro(dto);
                break;
            case TRANSFERENCIA:
                enviarTransferencia(dto);
                break;
            default:
                throw new RuntimeException("Tipo de transacción no soportado: " + dto.getTipoTransaccion());
        }
    }
}
