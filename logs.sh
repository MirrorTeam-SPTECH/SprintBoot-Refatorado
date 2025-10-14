#!/bin/bash

# ============================================
# Portal Churras - Script de Logs
# ============================================
# Este script exibe logs dos containers

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "============================================"
echo "    🍔 Portal Churras - Visualizar Logs    "
echo "============================================"
echo -e "${NC}"

# Verificar se foi passado um serviço específico
if [ -z "$1" ]; then
    echo -e "${YELLOW}Exibindo logs de todos os serviços...${NC}"
    echo -e "${BLUE}Use 'Ctrl+C' para sair${NC}"
    echo ""
    docker-compose logs -f
else
    SERVICE=$1
    echo -e "${YELLOW}Exibindo logs do serviço: ${GREEN}$SERVICE${NC}"
    echo -e "${BLUE}Use 'Ctrl+C' para sair${NC}"
    echo ""
    
    case $SERVICE in
        postgres|db|database)
            docker-compose logs -f postgres
            ;;
        rabbitmq|rabbit|mq)
            docker-compose logs -f rabbitmq
            ;;
        redis|cache)
            docker-compose logs -f redis
            ;;
        pgadmin)
            docker-compose logs -f pgadmin
            ;;
        *)
            echo -e "${RED}❌ Serviço desconhecido: $SERVICE${NC}"
            echo ""
            echo -e "${YELLOW}Serviços disponíveis:${NC}"
            echo -e "  - ${GREEN}postgres${NC} (ou db, database)"
            echo -e "  - ${GREEN}rabbitmq${NC} (ou rabbit, mq)"
            echo -e "  - ${GREEN}redis${NC} (ou cache)"
            echo -e "  - ${GREEN}pgadmin${NC}"
            echo ""
            echo -e "${YELLOW}Exemplo:${NC} ./logs.sh postgres"
            exit 1
            ;;
    esac
fi
