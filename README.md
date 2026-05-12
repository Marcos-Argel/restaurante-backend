# 🍽️ Restaurante Backend

API REST para sistema de gestión de restaurante desarrollada con **Java 21** y **Spring Boot 3.2.3**.

---

## 🚀 Tecnologías utilizadas

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

---

## 📋 Módulos del sistema

- 🔐 **Autenticación** con JWT y Spring Security
- 🪑 **Mesas** — gestión y disponibilidad
- 📦 **Pedidos y detalle de pedidos**
- 🛒 **Productos, categorías e ingredientes**
- 🏭 **Inventario y movimientos de stock**
- 🧾 **Facturas y compras**
- 🚚 **Proveedores**
- 📅 **Reservas**
- 💰 **Turnos de caja**
- 👥 **Usuarios y roles**

---

## 📂 Estructura del proyecto
src/main/java/com/restaurante/

├── config/         # Configuración general

├── controller/     # Endpoints REST

├── dto/            # Data Transfer Objects

├── entity/         # Entidades JPA

├── exception/      # Manejo de errores

├── repository/     # Acceso a datos

├── security/       # JWT y Spring Security

└── service/        # Lógica de negocio

---

## 🛠️ Instalación

### Requisitos
- Java 21
- MySQL 8.0
- Maven 3.6+
- Docker (opcional)

### Pasos

1. **Clonar el repositorio**
```bash
git clone https://github.com/Marcos-Argel/restaurante-backend.git
cd restaurante-backend
```

2. **Configurar base de datos** en `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/restaurante
spring.datasource.username=root
spring.datasource.password=tu_contraseña
```

3. **Ejecutar con Maven**
```bash
mvn clean install
mvn spring-boot:run
```

4. **O con Docker**
```bash
docker build -t restaurante-backend .
docker run -p 8080:8080 restaurante-backend
```

---

## 🔒 Seguridad

- Autenticación basada en **JWT**
- Control de acceso por **roles**
- Validación de datos en todos los endpoints

---

## 📄 Licencia

Proyecto educativo — Sistema de gestión para restaurante.
