#!/bin/bash

# ============================================
# Portal Churras - Script de Limpeza
# ============================================
# Este script limpa todos os containers, volumes e builds

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "============================================"
echo "    🍔 Portal Churras - Limpeza Completa    "
echo "============================================"
echo -e "${NC}"

echo -e "${RED}⚠️  ATENÇÃO: Esta ação irá:${NC}"
echo -e "  - Parar todos os containers"
echo -e "  - Remover todos os volumes (dados serão perdidos)"
echo -e "  - Limpar builds do Maven"
echo ""
read -p "Deseja continuar? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Operação cancelada.${NC}"
    exit 0
fi

# Parar e remover containers
echo -e "${YELLOW}🛑 Parando e removendo containers...${NC}"
docker-compose down -v

# Limpar builds do Maven
echo -e "${YELLOW}🧹 Limpando builds do Maven...${NC}"
if [ -f "./mvnw" ]; then
    ./mvnw clean
fi

# Remover target directory
if [ -d "target" ]; then
    echo -e "${YELLOW}🧹 Removendo diretório target...${NC}"
    rm -rf target
fi

echo ""
echo -e "${GREEN}✅ Limpeza concluída!${NC}"
echo ""
echo -e "${BLUE}Para iniciar novamente, execute: ${GREEN}./start.sh${NC}"
echo ""
