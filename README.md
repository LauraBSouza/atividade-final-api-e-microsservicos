# 🩺 Sistema de Agendamento de Consultas - Monolito e Microsserviços

## 📋 Visão Geral

Este projeto implementa um sistema completo de agendamento de consultas médicas com duas arquiteturas:

1. **Monolito** - Sistema completo e independente
2. **Microsserviços** - Arquitetura distribuída com serviços especializados

---

## 🏗️ Arquiteturas Disponíveis

### 🏢 Monolito (Porta 8080)
**Localização**: `backend-main/backend-main/consulta-facil-api/`

Sistema completo e independente que contém todas as funcionalidades em uma única aplicação:
- ✅ Autenticação e autorização
- ✅ Gestão de usuários (pacientes, profissionais, administradores)
- ✅ Gestão de horários disponíveis
- ✅ Agendamento de consultas
- ✅ Relatórios e histórico

**Tecnologias**: Spring Boot, Spring Security, JWT, MySQL

### 🔗 Microsserviços (Portas 8081-8083)
**Localização**: `microsservicos/migrados/`

Arquitetura distribuída com serviços especializados:

#### 🔐 Auth Service (Porta 8082)
- Autenticação e autorização
- Gestão de usuários e roles
- Validação de tokens JWT

#### 👤 User Service (Porta 8083)
- Gestão de perfis de usuários
- Informações de pacientes e profissionais
- Validação de permissões

#### 📅 Consulta Service (Porta 8081)
- Agendamento de consultas
- Gestão de horários (integração com monolito)
- Histórico de consultas

**Tecnologias**: Spring Boot, Spring Security, JWT, H2 (arquivo), RestTemplate

---

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven
- MySQL (para o monolito)

### 1. Executar o Monolito

```bash
cd backend-main/backend-main/consulta-facil-api
./mvnw spring-boot:run
```

**Configuração do banco** (MySQL):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/clinica_db
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
```

### 2. Executar os Microsserviços

```bash
# Auth Service
cd microsservicos/migrados/auth-service-main/auth-service-main
./mvnw spring-boot:run

# User Service  
cd microsservicos/migrados/user-service
./mvnw spring-boot:run

# Consulta Service
cd microsservicos/migrados/consulta-service
./mvnw spring-boot:run
```

### 3. Script de Inicialização (PowerShell)

```powershell
# Executar o script para inicializar dados padrão
./inicializar-sistema.ps1
```

---

## 🔄 Fluxo de Integração

### Monolito Independente
```
Cliente → Monolito (8080) → MySQL
```

### Microsserviços Integrados
```
Cliente → Consulta Service (8081) → User Service (8083) → Auth Service (8082)
                ↓
         Monolito (8080) - Horários
```

### Fluxo de Agendamento (Microsserviços)
1. **Autenticação**: Cliente obtém token do Auth Service
2. **Validação**: Consulta Service valida token com Auth Service
3. **Permissões**: Consulta Service verifica permissões com User Service
4. **Horários**: Consulta Service busca horários disponíveis do Monolito
5. **Agendamento**: Consulta Service salva a consulta em seu banco H2

---

## 🔐 Autenticação e Autorização

### Monolito
- JWT com chaves RSA
- Roles: `ROLE_PACIENTE`, `ROLE_PROFISSIONAL`, `ROLE_ADMIN`
- Endpoints protegidos por Spring Security

### Microsserviços
- JWT compartilhado entre serviços
- ACL (Access Control List) para validação de permissões
- Integração via RestTemplate

---

## 📊 Modelo de Dados

### Monolito (MySQL)
```sql
-- Usuários
usuarios (id, nome, email, senha, papel)

-- Horários
horarios (id, profissional_id, data_hora_inicio, data_hora_fim, disponivel)

-- Consultas  
consultas (id, paciente_id, profissional_id, horario, status)
```

### Microsserviços (H2)
```sql
-- Auth Service
users (id, username, password, roles)

-- User Service  
usuarios (id, nome, email, papel)

-- Consulta Service
consultas (id, paciente_id, profissional_id, horario, status)
```

---

## 🧪 Testando o Sistema

### 1. Cadastrar Usuários

**Monolito**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Paciente",
    "email": "joao@email.com", 
    "senha": "123456",
    "papel": "PACIENTE"
  }'
```

**Microsserviços**:
```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Paciente",
    "email": "joao@email.com",
    "senha": "123456", 
    "papel": "PACIENTE"
  }'
```

### 2. Fazer Login

**Monolito**:
```bash
curl -X POST http://localhost:8080/api/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@email.com",
    "senha": "123456"
  }'
```

**Microsserviços**:
```bash
curl -X POST http://localhost:8082/api/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@email.com",
    "senha": "123456"
  }'
```

### 3. Cadastrar Horários (Monolito)

```bash
curl -X POST http://localhost:8080/horarios \
  -H "Authorization: Bearer <token_profissional>" \
  -H "Content-Type: application/json" \
  -d '{
    "dataHoraInicio": "2025-07-08T14:00:00",
    "dataHoraFim": "2025-07-08T15:00:00",
    "profissionalId": 2
  }'
```

### 4. Agendar Consulta

**Monolito**:
```bash
curl -X POST http://localhost:8080/consultas \
  -H "Authorization: Bearer <token_paciente>" \
  -H "Content-Type: application/json" \
  -d '{
    "horario": "2025-07-08T14:00:00",
    "pacienteId": 3,
    "profissionalId": 2,
    "statusConsulta": "AGENDADA"
  }'
```

**Microsserviços**:
```bash
curl -X POST http://localhost:8081/api/consultas \
  -H "Authorization: Bearer <token_paciente>" \
  -H "Content-Type: application/json" \
  -d '{
    "horario": "2025-07-08T14:00:00",
    "pacienteId": 3,
    "profissionalId": 2,
    "statusConsulta": "AGENDADA"
  }'
```

---

## 🔧 Configurações Importantes

### Monolito
- **Porta**: 8080
- **Banco**: MySQL
- **Endpoints públicos**: `/horarios/**`, `/api/auth/**`, `/swagger-ui/**`

### Microsserviços
- **Auth Service**: Porta 8082
- **User Service**: Porta 8083  
- **Consulta Service**: Porta 8081
- **Banco**: H2 (arquivo local)
- **Integração**: RestTemplate para comunicação entre serviços

---

## 📚 Documentação

### Swagger UI
- **Monolito**: http://localhost:8080/swagger-ui.html
- **Microsserviços**: Cada serviço tem sua própria documentação

### Postman Collection
- **Monolito**: `docs/postman/consulta-facil-api.postman_collection.json`

---

## 🐛 Troubleshooting

### Problemas Comuns

1. **Erro 401 no login**
   - Verificar se o AuthenticationManager está configurado
   - Verificar se o PasswordEncoder está sendo usado corretamente

2. **Erro 403 ao agendar consultas**
   - Verificar se o usuário tem a role `PACIENTE`
   - Verificar se o endpoint está permitido para pacientes

3. **Erro de deserialização JSON**
   - Verificar se os DTOs estão corretos
   - Verificar se o RestTemplate está configurado

4. **Horários não encontrados**
   - Verificar se os horários foram cadastrados no monolito
   - Verificar se a integração entre microsserviço e monolito está funcionando

---

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

---

## 👥 Autores

- **Equipe de Desenvolvimento** - *Trabalho inicial* - [IFSP]

---

© 2025 - Sistema de Agendamento de Consultas - Monolito e Microsserviços 