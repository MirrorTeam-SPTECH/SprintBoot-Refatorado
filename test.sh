#!/bin/bash

# ============================================
# Portal Churras - Script de Testes
# ============================================
# Este script executa os testes do projeto

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "============================================"
echo "    🍔 Portal Churras - Executar Testes    "
echo "============================================"
echo -e "${NC}"

# Verificar se Maven está disponível
if [ ! -f "./mvnw" ]; then
    echo -e "${RED}❌ Maven Wrapper (mvnw) não encontrado!${NC}"
    exit 1
fi

# Executar testes
echo -e "${BLUE}🧪 Executando testes...${NC}"
./mvnw test

echo ""
echo -e "${GREEN}✅ Testes concluídos!${NC}"
echo ""
echo -e "${BLUE}📊 Relatório de cobertura em:${NC} target/site/jacoco/index.html"
echo ""
