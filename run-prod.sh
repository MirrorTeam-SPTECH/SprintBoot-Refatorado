#!/bin/bash

# ============================================
# Portal Churras - Script de Execução (Prod)
# ============================================
# Este script executa a aplicação em modo produção

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "============================================"
echo "    🍔 Portal Churras - Modo Produção    "
echo "============================================"
echo -e "${NC}"

# Verificar se o JAR existe
if [ ! -f "target/PortalChurras-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${YELLOW}⚠️  JAR não encontrado!${NC}"
    echo -e "${YELLOW}Executando build...${NC}"
    ./package.sh
fi

# Verificar se os serviços Docker estão rodando
if ! docker-compose ps | grep -q "Up"; then
    echo -e "${YELLOW}⚠️  Serviços Docker não estão rodando!${NC}"
    echo -e "${YELLOW}Iniciando serviços...${NC}"
    ./start.sh
fi

# Verificar variáveis de ambiente críticas
if [ -z "$MERCADO_PAGO_ACCESS_TOKEN" ]; then
    echo -e "${RED}❌ MERCADO_PAGO_ACCESS_TOKEN não configurado!${NC}"
    echo -e "${YELLOW}Por favor, configure as variáveis de ambiente necessárias.${NC}"
    exit 1
fi

if [ -z "$JWT_SECRET" ]; then
    echo -e "${YELLOW}⚠️  JWT_SECRET não configurado! Usando valor padrão (NÃO RECOMENDADO PARA PRODUÇÃO)${NC}"
fi

echo ""
echo -e "${BLUE}🚀 Iniciando aplicação em modo produção...${NC}"
echo -e "${YELLOW}Profile ativo: ${GREEN}prod${NC}"
echo ""
echo -e "${BLUE}📍 A aplicação estará disponível em:${NC}"
echo -e "  ${GREEN}http://localhost:8080${NC}"
echo ""
echo -e "${YELLOW}Pressione Ctrl+C para parar a aplicação${NC}"
echo ""

# Executar aplicação
java -jar -Dspring.profiles.active=prod target/PortalChurras-0.0.1-SNAPSHOT.jar
