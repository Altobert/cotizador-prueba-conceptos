#!/bin/bash

# Script para ejecutar el Visor de Cotizaciones JavaFX

cd "$(dirname "$0")"

echo "==================================="
echo "  VISOR DE COTIZACIONES VSS"
echo "==================================="
echo ""
echo "Compilando proyecto..."
mvn compile

if [ $? -eq 0 ]; then
    echo ""
    echo "Iniciando aplicación JavaFX..."
    echo ""
    mvn exec:java -Dexec.mainClass="cl.vsschile.CotizacionViewerApp"
else
    echo ""
    echo "❌ Error en la compilación"
    exit 1
fi
