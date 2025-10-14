#!/bin/bash

# ============================================
# Portal Churras - Script de Package
# ============================================
# Este script empacota o projeto para produção

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "============================================"
echo "  🍔 Portal Churras - Package do Projeto  "
echo "============================================"
echo -e "${NC}"

# Verificar se Maven está disponível
if [ ! -f "./mvnw" ]; then
    echo -e "${RED}❌ Maven Wrapper (mvnw) não encontrado!${NC}"
    exit 1
fi

# Executar testes e empacotar
echo -e "${BLUE}📦 Empacotando o projeto (com testes)...${NC}"
./mvnw clean package

# Verificar se o JAR foi criado
if [ -f "target/PortalChurras-0.0.1-SNAPSHOT.jar" ]; then
    echo ""
    echo -e "${GREEN}============================================${NC}"
    echo -e "${GREEN}✅ Empacotamento concluído com sucesso!${NC}"
    echo -e "${GREEN}============================================${NC}"
    echo ""
    echo -e "${BLUE}📦 JAR criado em:${NC} target/PortalChurras-0.0.1-SNAPSHOT.jar"
    echo ""
    echo -e "${YELLOW}Para executar em produção:${NC}"
    echo -e "  ${GREEN}java -jar -Dspring.profiles.active=prod target/PortalChurras-0.0.1-SNAPSHOT.jar${NC}"
    echo ""
else
    echo -e "${RED}❌ Erro ao criar o JAR!${NC}"
    exit 1
fi
