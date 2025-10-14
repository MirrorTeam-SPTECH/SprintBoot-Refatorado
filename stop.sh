#!/bin/bash

# ============================================
# Portal Churras - Script de Parada
# ============================================
# Este script para todos os serviços

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "============================================"
echo "    🍔 Portal Churras - Parando Serviços    "
echo "============================================"
echo -e "${NC}"

# Parar containers
echo -e "${YELLOW}🛑 Parando containers...${NC}"
docker-compose down

echo ""
echo -e "${GREEN}✅ Todos os serviços foram parados!${NC}"
echo ""
echo -e "${BLUE}Para iniciar novamente, execute: ${GREEN}./start.sh${NC}"
echo ""
