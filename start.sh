#!/bin/bash

# ============================================
# Portal Churras - Script de Inicialização
# ============================================
# Este script inicia todos os serviços necessários

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "============================================"
echo "    🍔 Portal Churras - Inicialização    "
echo "============================================"
echo -e "${NC}"

# Verificar se Docker está instalado
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker não está instalado!${NC}"
    echo "Por favor, instale o Docker: https://www.docker.com/get-started"
    exit 1
fi

# Verificar se Docker Compose está instalado
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose não está instalado!${NC}"
    echo "Por favor, instale o Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi

# Verificar se arquivo .env existe
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠️  Arquivo .env não encontrado!${NC}"
    echo -e "${YELLOW}📄 Criando .env a partir do .env.example...${NC}"
    if [ -f .env.example ]; then
        cp .env.example .env
        echo -e "${GREEN}✅ Arquivo .env criado!${NC}"
        echo -e "${YELLOW}⚠️  Por favor, edite o arquivo .env com suas configurações antes de continuar.${NC}"
        exit 0
    else
        echo -e "${RED}❌ Arquivo .env.example não encontrado!${NC}"
        exit 1
    fi
fi

# Parar containers anteriores (se existirem)
echo -e "${YELLOW}🛑 Parando containers anteriores...${NC}"
docker-compose down 2>/dev/null || true

# Iniciar serviços de infraestrutura
echo -e "${BLUE}🚀 Iniciando serviços de infraestrutura (PostgreSQL, RabbitMQ, Redis)...${NC}"
docker-compose up -d

# Aguardar serviços ficarem prontos
echo -e "${YELLOW}⏳ Aguardando serviços ficarem prontos...${NC}"
sleep 10

# Verificar status dos containers
echo -e "${BLUE}📊 Status dos containers:${NC}"
docker-compose ps

# Verificar se PostgreSQL está pronto
echo -e "${YELLOW}🔍 Verificando PostgreSQL...${NC}"
until docker-compose exec -T postgres pg_isready -U admin -d portalchurras 2>/dev/null; do
    echo -e "${YELLOW}⏳ Aguardando PostgreSQL...${NC}"
    sleep 2
done
echo -e "${GREEN}✅ PostgreSQL está pronto!${NC}"

# Verificar se RabbitMQ está pronto
echo -e "${YELLOW}🔍 Verificando RabbitMQ...${NC}"
sleep 5
echo -e "${GREEN}✅ RabbitMQ está pronto!${NC}"

# Verificar se Redis está pronto
echo -e "${YELLOW}🔍 Verificando Redis...${NC}"
until docker-compose exec -T redis redis-cli ping 2>/dev/null | grep -q PONG; do
    echo -e "${YELLOW}⏳ Aguardando Redis...${NC}"
    sleep 2
done
echo -e "${GREEN}✅ Redis está pronto!${NC}"

echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}✅ Serviços de infraestrutura iniciados!${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo -e "${BLUE}📋 Informações dos Serviços:${NC}"
echo ""
echo -e "  ${BLUE}PostgreSQL:${NC}"
echo -e "    - Host: localhost:5432"
echo -e "    - Database: portalchurras"
echo -e "    - User: admin"
echo -e "    - Password: admin123"
echo ""
echo -e "  ${BLUE}RabbitMQ:${NC}"
echo -e "    - AMQP: localhost:5672"
echo -e "    - Management UI: http://localhost:15672"
echo -e "    - User: admin"
echo -e "    - Password: admin123"
echo ""
echo -e "  ${BLUE}Redis:${NC}"
echo -e "    - Host: localhost:6379"
echo ""
echo -e "  ${BLUE}pgAdmin:${NC}"
echo -e "    - URL: http://localhost:5050"
echo -e "    - Email: admin@portalchurras.com"
echo -e "    - Password: admin123"
echo ""
echo -e "${YELLOW}📌 Próximos passos:${NC}"
echo -e "  1. Execute: ${GREEN}./mvnw spring-boot:run${NC}"
echo -e "  2. Acesse a API: ${GREEN}http://localhost:8080${NC}"
echo -e "  3. Swagger UI: ${GREEN}http://localhost:8080/swagger-ui.html${NC}"
echo ""
echo -e "${BLUE}Para parar os serviços, execute: ${GREEN}./stop.sh${NC}"
echo ""
