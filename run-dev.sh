#!/bin/bash

# ============================================
# Portal Churras - Script de Execução (Dev)
# ============================================
# Este script executa a aplicação em modo desenvolvimento

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "============================================"
echo "  🍔 Portal Churras - Modo Desenvolvimento  "
echo "============================================"
echo -e "${NC}"

# Verificar se Maven está disponível
if [ ! -f "./mvnw" ]; then
    echo -e "${RED}❌ Maven Wrapper (mvnw) não encontrado!${NC}"
    exit 1
fi

# Verificar se os serviços Docker estão rodando
if ! docker-compose ps | grep -q "Up"; then
    echo -e "${YELLOW}⚠️  Serviços Docker não estão rodando!${NC}"
    echo -e "${YELLOW}Iniciando serviços...${NC}"
    ./start.sh
fi

echo ""
echo -e "${BLUE}🚀 Iniciando aplicação em modo desenvolvimento...${NC}"
echo -e "${YELLOW}Profile ativo: ${GREEN}development${NC}"
echo ""
echo -e "${BLUE}📍 A aplicação estará disponível em:${NC}"
echo -e "  ${GREEN}http://localhost:8080${NC}"
echo ""
echo -e "${BLUE}📖 Swagger UI:${NC}"
echo -e "  ${GREEN}http://localhost:8080/swagger-ui.html${NC}"
echo ""
echo -e "${BLUE}💾 H2 Console:${NC}"
echo -e "  ${GREEN}http://localhost:8080/h2-console${NC}"
echo ""
echo -e "${YELLOW}Pressione Ctrl+C para parar a aplicação${NC}"
echo ""

# Executar aplicação
./mvnw spring-boot:run
