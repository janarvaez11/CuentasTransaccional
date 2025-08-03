package com.banquito.core.cuentas.producer;

import com.banquito.core.cuentas.dto.TransaccionesSolicitudDTO;
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
    private final String transferenciaQueue;

    public TransaccionesProducer(
            JmsTemplate jmsTemplate,
            @Value("${colas.transacciones.deposito:transacciones.deposito}") String depositoQueue,
            @Value("${colas.transacciones.retiro:transacciones.retiro}") String retiroQueue,
            @Value("${colas.transacciones.transferencia:transacciones.transferencia}") String transferenciaQueue) {
        this.jmsTemplate = jmsTemplate;
        this.depositoQueue = depositoQueue;
        this.retiroQueue = retiroQueue;
        this.transferenciaQueue = transferenciaQueue;
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
            log.info("Enviando TRANSFERENCIA a cola: {} de {} a {}", 
                    transferenciaQueue, dto.getNumeroCuentaOrigen(), dto.getNumeroCuentaDestino());
            jmsTemplate.convertAndSend(transferenciaQueue, dto);
            log.info("Transferencia enviada exitosamente a cola");
        } catch (Exception e) {
            log.error("Error enviando transferencia a cola: {}", e.getMessage(), e);
            throw new RuntimeException("Error enviando transferencia a cola", e);
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
