import requests
import concurrent.futures
import random
import time

# Endpoints disponibles
ENDPOINTS = {
    "TRANSFERENCIA": "http://localhost:8080/api/v1/transacciones/transferencia",
    "RETIRO": "http://localhost:8080/api/v1/transacciones/retiro",
    "DEPOSITO": "http://localhost:8080/api/v1/transacciones/deposito"
}

# Generar payload según tipo de transacción
def generar_payload(tipo, n):
    if tipo == "TRANSFERENCIA":
        return {
            "numeroCuentaOrigen": "7777777777",
            "numeroCuentaDestino": "1111111111",
            "tipoTransaccion": tipo,
            "monto": round(random.uniform(10.00, 1000.00), 2),
            "descripcion": f"Transferencia {n}"
        }
    elif tipo == "RETIRO":
        return {
            "numeroCuentaOrigen": "1111111111",
            "tipoTransaccion": tipo,
            "monto": round(random.uniform(10.00, 500.00), 2),
            "descripcion": f"Retiro {n}"
        }
    elif tipo == "DEPOSITO":
        return {
            "numeroCuentaOrigen": "2222222222",
            "tipoTransaccion": tipo,
            "monto": round(random.uniform(100.00, 50000.00), 2),
            "descripcion": f"Depósito {n}"
        }

# Ejecutar la petición
def enviar_peticion(n):
    tipo = random.choice(list(ENDPOINTS.keys()))
    url = ENDPOINTS[tipo]
    payload = generar_payload(tipo, n)

    try:
        response = requests.post(url, json=payload)
        print(f"[{n}] {tipo} => Código: {response.status_code}")
    except Exception as e:
        print(f"[{n}] {tipo} => Error: {e}")

# Ejecutar 100 peticiones concurrentes
def main():
    inicio = time.time()
    with concurrent.futures.ThreadPoolExecutor(max_workers=20) as executor:
        executor.map(enviar_peticion, range(1, 51))
    fin = time.time()
    print(f"\nCompletadas en {round(fin - inicio, 2)} segundos.")

if __name__ == "__main__":
    main()
