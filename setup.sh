#!/bin/bash

# ============================================
# Portal Churras - Script de Setup Inicial
# ============================================
# Este script configura o projeto pela primeira vez

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

clear

echo -e "${BLUE}"
echo "============================================"
echo "  🍔 Portal Churras - Setup Inicial  "
echo "============================================"
echo -e "${NC}"

# Verificar pré-requisitos
echo -e "${BLUE}📋 Verificando pré-requisitos...${NC}"
echo ""

# Verificar Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
    if [ "$JAVA_VERSION" -ge 21 ]; then
        echo -e "${GREEN}✅ Java $JAVA_VERSION encontrado${NC}"
    else
        echo -e "${RED}❌ Java 21 ou superior é necessário (encontrado: $JAVA_VERSION)${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ Java não encontrado!${NC}"
    echo "Por favor, instale Java 21 ou superior: https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi

# Verificar Maven
if [ -f "./mvnw" ]; then
    echo -e "${GREEN}✅ Maven Wrapper encontrado${NC}"
else
    echo -e "${RED}❌ Maven Wrapper (mvnw) não encontrado!${NC}"
    exit 1
fi

# Verificar Docker
if command -v docker &> /dev/null; then
    echo -e "${GREEN}✅ Docker encontrado${NC}"
else
    echo -e "${RED}❌ Docker não encontrado!${NC}"
    echo "Por favor, instale Docker: https://www.docker.com/get-started"
    exit 1
fi

# Verificar Docker Compose
if command -v docker-compose &> /dev/null; then
    echo -e "${GREEN}✅ Docker Compose encontrado${NC}"
else
    echo -e "${RED}❌ Docker Compose não encontrado!${NC}"
    echo "Por favor, instale Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi

echo ""
echo -e "${GREEN}✅ Todos os pré-requisitos foram atendidos!${NC}"
echo ""

# Configurar arquivo .env
if [ ! -f .env ]; then
    echo -e "${YELLOW}📝 Configurando arquivo .env...${NC}"
    if [ -f .env.example ]; then
        cp .env.example .env
        echo -e "${GREEN}✅ Arquivo .env criado a partir do .env.example${NC}"
        echo ""
        echo -e "${YELLOW}⚠️  IMPORTANTE: Configure as seguintes variáveis no arquivo .env:${NC}"
        echo -e "  - ${BLUE}MERCADO_PAGO_ACCESS_TOKEN${NC}"
        echo -e "  - ${BLUE}MERCADO_PAGO_PUBLIC_KEY${NC}"
        echo -e "  - ${BLUE}JWT_SECRET${NC} (use uma chave forte!)"
        echo ""
        read -p "Deseja editar o arquivo .env agora? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            ${EDITOR:-nano} .env
        fi
    else
        echo -e "${RED}❌ Arquivo .env.example não encontrado!${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}✅ Arquivo .env já existe${NC}"
fi

echo ""
echo -e "${BLUE}🔨 Compilando o projeto...${NC}"
./mvnw clean compile

echo ""
echo -e "${GREEN}"
echo "============================================"
echo "  ✅ Setup concluído com sucesso!"
echo "============================================"
echo -e "${NC}"
echo ""
echo -e "${BLUE}📋 Próximos passos:${NC}"
echo ""
echo -e "  1. ${YELLOW}Inicie os serviços de infraestrutura:${NC}"
echo -e "     ${GREEN}./start.sh${NC}"
echo ""
echo -e "  2. ${YELLOW}Execute a aplicação em modo desenvolvimento:${NC}"
echo -e "     ${GREEN}./run-dev.sh${NC}"
echo ""
echo -e "  3. ${YELLOW}Acesse a aplicação:${NC}"
echo -e "     API: ${GREEN}http://localhost:8080${NC}"
echo -e "     Swagger: ${GREEN}http://localhost:8080/swagger-ui.html${NC}"
echo ""
echo -e "${BLUE}📚 Scripts disponíveis:${NC}"
echo -e "  ${GREEN}./start.sh${NC}     - Inicia os serviços Docker"
echo -e "  ${GREEN}./stop.sh${NC}      - Para os serviços Docker"
echo -e "  ${GREEN}./run-dev.sh${NC}   - Executa a aplicação (dev)"
echo -e "  ${GREEN}./run-prod.sh${NC}  - Executa a aplicação (prod)"
echo -e "  ${GREEN}./build.sh${NC}     - Compila o projeto"
echo -e "  ${GREEN}./package.sh${NC}   - Empacota o projeto"
echo -e "  ${GREEN}./test.sh${NC}      - Executa os testes"
echo -e "  ${GREEN}./logs.sh${NC}      - Exibe logs dos containers"
echo -e "  ${GREEN}./clean.sh${NC}     - Limpa tudo (containers e builds)"
echo ""
