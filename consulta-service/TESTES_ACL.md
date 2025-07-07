# 🧪 Guia de Testes da ACL - Comunicação entre Microsserviços

## 📋 Pré-requisitos

1. **Monolito (consulta-facil-api)** rodando na porta 8080
2. **Microsserviço (consulta-service)** rodando na porta 8081
3. **Postman** ou **curl** para fazer requisições

## 🚀 Iniciando os Serviços

### 1. Iniciar o Monolito:
```bash
cd backend-main/backend-main/consulta-facil-api
mvn spring-boot:run
```

### 2. Iniciar o Microsserviço:
```bash
cd microsservicos/migrados/consulta-service
mvn spring-boot:run
```

## 🧪 Sequência de Testes

### **Teste 1: Verificar se os serviços estão rodando**

#### 1.1 - Testar Monolito (Swagger):
```bash
curl -X GET "http://localhost:8080/v3/api-docs" -H "accept: application/json"
```

#### 1.2 - Testar Microsserviço (Swagger):
```bash
curl -X GET "http://localhost:8081/v3/api-docs" -H "accept: application/json"
```

### **Teste 2: Login no Monolito para obter Token**

#### 2.1 - Fazer login como admin:
```bash
curl -X POST "http://localhost:8080/api/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@consulta.com",
    "senha": "admin123"
  }'
```

**Resposta esperada:**
```json
"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 2.2 - Fazer login como paciente:
```bash
curl -X POST "http://localhost:8080/api/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "paciente@consulta.com",
    "senha": "paciente123"
  }'
```

### **Teste 3: Validar Token via ACL (Endpoint do Monolito)**

#### 3.1 - Validar token válido:
```bash
curl -X GET "http://localhost:8080/api/auth/validate" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Resposta esperada:**
```json
{
  "id": 1,
  "username": "admin@consulta.com",
  "email": "admin@consulta.com",
  "roles": ["ROLE_ADMIN"]
}
```

#### 3.2 - Testar token inválido:
```bash
curl -X GET "http://localhost:8080/api/auth/validate" \
  -H "Authorization: Bearer token_invalido"
```

**Resposta esperada:** `401 Unauthorized`

### **Teste 4: Testar ACL no Microsserviço**

#### 4.1 - Acessar endpoint protegido com token válido:
```bash
curl -X GET "http://localhost:8081/api/consultas" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

#### 4.2 - Acessar endpoint protegido sem token:
```bash
curl -X GET "http://localhost:8081/api/consultas"
```

**Resposta esperada:** `401 Unauthorized`

#### 4.3 - Testar endpoint de consultas por paciente com permissões:
```bash
curl -X GET "http://localhost:8081/api/consultas/paciente/1" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

### **Teste 5: Testar Diferentes Roles**

#### 5.1 - Testar como ADMIN (acesso total):
```bash
# Login como admin
TOKEN_ADMIN=$(curl -s -X POST "http://localhost:8080/api/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@consulta.com", "senha": "admin123"}')

# Acessar qualquer consulta
curl -X GET "http://localhost:8081/api/consultas/paciente/1" \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

#### 5.2 - Testar como PACIENTE (acesso restrito):
```bash
# Login como paciente
TOKEN_PACIENTE=$(curl -s -X POST "http://localhost:8080/api/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{"email": "paciente@consulta.com", "senha": "paciente123"}')

# Tentar acessar consultas de outro paciente (deve falhar)
curl -X GET "http://localhost:8081/api/consultas/paciente/999" \
  -H "Authorization: Bearer $TOKEN_PACIENTE"
```

## 🔍 Verificando Logs

### Monolito:
```bash
# Ver logs do monolito
tail -f logs/consulta-facil-api.log
```

### Microsserviço:
```bash
# Ver logs do microsserviço
tail -f logs/consulta-service.log
```

## 🐛 Troubleshooting

### Problema: "Connection refused"
- Verificar se os serviços estão rodando
- Verificar as portas (8080 e 8081)

### Problema: "Token inválido"
- Verificar se o token não expirou
- Verificar se o formato está correto (Bearer + token)

### Problema: "401 Unauthorized"
- Verificar se o endpoint está protegido
- Verificar se o token tem as permissões necessárias

## 📊 Testes com Postman

### Collection para importar:
```json
{
  "info": {
    "name": "ACL Tests",
    "description": "Testes da ACL entre microsserviços"
  },
  "item": [
    {
      "name": "Login Admin",
      "request": {
        "method": "POST",
        "url": "http://localhost:8080/api/auth/authenticate",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"admin@consulta.com\",\n  \"senha\": \"admin123\"\n}"
        }
      }
    },
    {
      "name": "Validate Token",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/api/auth/validate",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ]
      }
    },
    {
      "name": "Access Microservice",
      "request": {
        "method": "GET",
        "url": "http://localhost:8081/api/consultas",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ]
      }
    }
  ]
}
```

## ✅ Checklist de Testes

- [ ] Monolito inicia sem erros
- [ ] Microsserviço inicia sem erros
- [ ] Login retorna token válido
- [ ] Validação de token funciona
- [ ] ACL valida tokens corretamente
- [ ] Diferentes roles têm permissões corretas
- [ ] Endpoints protegidos funcionam
- [ ] Logs mostram comunicação entre serviços 