@echo off
REM Abre el tunel de ngrok con la direccion fija del proyecto, apuntando al backend (puerto 8080).
REM Mercado Pago manda los avisos de pago a esta direccion (configurada una sola vez en el panel,
REM en Webhooks: https://ineffable-earlene-ironically.ngrok-free.dev/webhooks/mercadopago).
REM Dejar esta ventana abierta mientras se usa el sistema.
ngrok http 8080 --url=https://ineffable-earlene-ironically.ngrok-free.dev
