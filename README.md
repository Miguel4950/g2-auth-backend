# Sistema de Biblioteca Digital - Grupo 2: Autenticación y Gestión de Usuarios

## 📋 Descripción
Este módulo implementa el sistema completo de autenticación y gestión de usuarios para el Sistema de Biblioteca Digital, desarrollado como parte del proyecto semestral de Fundamentos de Ingeniería de Software.

## 👥 Integrantes del Grupo 2
- Nicolás Castañeda
- Nicolás León  
- Juan Moreno
- Samuel Nemes
- Luna Rengifo

## 🚀 Tecnologías Utilizadas
- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Security**
- **JWT (JSON Web Tokens)**
- **H2 Database** (Base de datos en memoria)
- **BCrypt** (Encriptación de contraseñas)
- **Swagger/OpenAPI** (Documentación de APIs)
- **Maven** (Gestión de dependencias)

## 📁 Estructura del Proyecto

```
prestamos/
├── src/
│   ├── main/
│   │   ├── java/co/edu/javeriana/prestamos/
│   │   │   ├── config/          # Configuraciones (Security, OpenAPI)
│   │   │   ├── controller/      # Controladores REST
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── exception/      # Excepciones personalizadas
│   │   │   ├── model/          # Entidades JPA
│   │   │   ├── repository/     # Repositorios JPA
│   │   │   ├── security/       # JWT y autenticación
│   │   │   └── service/        # Lógica de negocio
│   │   └── resources/
│   │       ├── application.properties  # Configuración
│   │       └── data.sql               # Datos iniciales
│   └── test/                   # Pruebas unitarias e integración
├── pom.xml                     # Dependencias Maven
└── README.md                   # Este archivo
```

## 🔧 Instalación y Configuración

### Prerequisitos
- Java 17 o superior
- Maven 3.6 o superior

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone [URL_DEL_REPOSITORIO]
cd prestamos
```

2. **Instalar dependencias**
```bash
mvn clean install
```

3. **Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## 📚 APIs Implementadas

### Endpoints de Autenticación

#### POST /api/auth/register
Registra un nuevo usuario en el sistema.

**Request Body:**
```json
{
  "nombre": "Juan Pérez",
  "username": "jperez",
  "email": "juan@email.com",
  "contrasena": "contrasena123",
  "id_tipo_usuario": 1
}
```

#### POST /api/auth/login
Autentica un usuario y retorna un token JWT.

**Request Body:**
```json
{
  "username": "jperez",
  "contrasena": "contrasena123"
}
```

**Response:**
```json
{
  "id_usuario": 1,
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "mensaje": "Login exitoso",
  "usuario_info": {
    "id_usuario": 1,
    "nombre": "Juan Pérez",
    "username": "jperez",
    "email": "juan@email.com",
    "id_tipo_usuario": 1,
    "tipo_usuario": "Estudiante"
  },
  "permisos": ["VER_CATALOGO", "SOLICITAR_PRESTAMO", "VER_MIS_PRESTAMOS"]
}
```

### Endpoints de Gestión de Usuarios

#### GET /api/users/profile
Obtiene el perfil del usuario autenticado.
**Requiere:** Token JWT

#### PUT /api/users/profile
Actualiza el perfil del usuario.
**Requiere:** Token JWT

#### PUT /api/users/change-contrasena
Cambia la contraseña del usuario.
**Requiere:** Token JWT

#### GET /api/users/{id}
Obtiene información de un usuario específico.
**Requiere:** Token JWT + Rol Bibliotecario

#### GET /api/users
Lista todos los usuarios con paginación.
**Requiere:** Token JWT + Rol Bibliotecario

#### PUT /api/users/{id}/activate
Activa un usuario.
**Requiere:** Token JWT + Rol Admin

#### PUT /api/users/{id}/deactivate
Desactiva un usuario.
**Requiere:** Token JWT + Rol Admin

## 🔐 Seguridad

### Autenticación JWT
- Los tokens JWT tienen una duración de 24 horas
- El token debe enviarse en el header `Authorization: Bearer {token}`

### Roles y Permisos
1. **Estudiante**: Acceso básico al sistema
2. **Bibliotecario**: Gestión de préstamos y usuarios
3. **Admin**: Control total del sistema

### Encriptación
- Contraseñas encriptadas con BCrypt
- 10 rounds de salt por defecto

## 🧪 Pruebas

### Ejecutar todas las pruebas
```bash
mvn test
```

### Usuarios de Prueba
| Username | contrasena | Rol |
|----------|----------|-----|
| admin | contrasena123 | Admin |
| jperez | contrasena123 | Bibliotecario |
| amartinez | contrasena123 | Estudiante |

## 📖 Documentación de APIs

### Swagger UI
Acceder a: `http://localhost:8080/swagger-ui.html`

### OpenAPI Spec
Disponible en: `http://localhost:8080/v3/api-docs`

## 🗄️ Base de Datos

### Acceso a H2 Console
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:biblioteca_db`
- Username: `sa`
- contrasena: (vacío)

## 📝 Colección Postman
Se incluye el archivo `Biblioteca_Grupo2_Auth.postman_collection.json` con todos los endpoints documentados y listos para probar.

## 🔄 Integración con Otros Grupos

### Dependencias
- **Grupo 1**: Capa de persistencia (UsuarioRepository)
- **Grupo 3**: Validación de permisos para gestión de catálogo
- **Grupo 4**: Autenticación para sistema de préstamos
- **Grupo 5**: Token JWT para frontend

### Datos Compartidos
```java
// Credenciales de conexión BD (provistas por Grupo 1)
Host: localhost
DB: biblioteca_db
User: sa
Pass: (vacío)

// Token JWT format
Header.Authorization: Bearer {jwt_token}
```

## ⚠️ Manejo de Errores

### Códigos de Estado HTTP
- `200 OK`: Operación exitosa
- `201 CREATED`: Recurso creado exitosamente
- `400 BAD REQUEST`: Datos inválidos
- `401 UNAUTHORIZED`: Credenciales inválidas
- `403 FORBIDDEN`: Sin permisos suficientes
- `404 NOT FOUND`: Recurso no encontrado
- `409 CONFLICT`: Conflicto (ej: username ya existe)

### Respuesta de Error Estándar
```json
{
  "error": "Descripción del error"
}
```

## 📊 Métricas y Validaciones

### Reglas de Negocio Implementadas
- Username único y obligatorio
- Email único y formato válido
- Contraseña mínimo 6 caracteres con al menos 1 número
- Máximo 5 intentos fallidos de login antes de bloqueo
- Tipos de usuario válidos: 1 (Estudiante), 2 (Bibliotecario), 3 (Admin)

## 🐛 Solución de Problemas Comunes

### Error: "Token JWT inválido"
- Verificar que el token no haya expirado
- Asegurar que se incluya "Bearer " antes del token

### Error: "Cuenta bloqueada"
- Usuario tiene 5 o más intentos fallidos
- Contactar admin para desbloqueo

### Error: "Username ya existe"
- Elegir un username diferente
- Verificar en BD si ya existe

## 📈 Próximas Mejoras
- [ ] Implementar refresh token
- [ ] Agregar autenticación con OAuth2
- [ ] Implementar 2FA
- [ ] Añadir auditoría de acciones
- [ ] Mejorar recuperación de contraseña con envío real de email

## 📞 Contacto
Para dudas o problemas con este módulo, contactar al Grupo 2 de Autenticación.

---
**Última actualización:** Noviembre 2024
**Versión:** 1.0.0
