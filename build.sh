#!/bin/bash

# ============================================
# Portal Churras - Script de Build
# ============================================
# Este script compila o projeto

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "============================================"
echo "    🍔 Portal Churras - Build do Projeto    "
echo "============================================"
echo -e "${NC}"

# Verificar se Maven está disponível
if [ ! -f "./mvnw" ]; then
    echo -e "${RED}❌ Maven Wrapper (mvnw) não encontrado!${NC}"
    exit 1
fi

# Limpar builds anteriores
echo -e "${YELLOW}🧹 Limpando builds anteriores...${NC}"
./mvnw clean

# Compilar o projeto
echo -e "${BLUE}🔨 Compilando o projeto...${NC}"
./mvnw compile

echo ""
echo -e "${GREEN}✅ Compilação concluída com sucesso!${NC}"
echo ""
