<p align="center">
    <img src="https://medical-visor-bucket.s3.us-east-2.amazonaws.com/ChatGPT_Image_14_abr_2025__18_03_35-removebg-preview.png" align="center" width="30%">
</p>
<h1 align="center">MEDICALTESTVISOR</h1>
<p align="center">
	<em>A modular microservices-based platform to manage and visualize medical test data</em>
</p>
<p align="center">
	<img src="https://img.shields.io/github/license/DiegoBustos16/MedicalTestVisor?style=default&logo=MIT&logoColor=white&color=ff4800" alt="license">
	<img src="https://img.shields.io/github/last-commit/DiegoBustos16/MedicalTestVisor?style=default&logo=git&logoColor=white&color=ff4800" alt="last-commit">
	<img src="https://img.shields.io/github/languages/top/DiegoBustos16/MedicalTestVisor?style=default&color=ff4800" alt="repo-top-language">
	<img src="https://img.shields.io/github/languages/count/DiegoBustos16/MedicalTestVisor?style=default&color=ff4800" alt="repo-language-count">
</p>

---

## 📊 Overview

MedicalTestVisor is a full-stack distributed system that enables storage, management, and secure access to medical test data such as patient records, hospitals, doctors, and test results. Built on a microservices architecture, it ensures modularity, scalability, and ease of maintenance.

It includes:
- Role-based secure access via Keycloak
- Microservices for core domains (patients, doctors, hospitals, tests)
- RESTful APIs with Swagger/OpenAPI integration
- Centralized configuration and service discovery

---

## 🌟 Features

- ✅ Secure microservices with OAuth2/JWT-based authentication
- ✅ CRUD operations for Patients, Doctors, Hospitals, and Medical Tests
- ✅ Amazon S3 integration for uploading and retrieving medical images/files
- ✅ Docker-based deployment
- ✅ OpenAPI (Swagger UI) documentation
- ✅ Modular architecture following clean code principles
- ✅ Integrated unit testing across services

---

## 📁 Project Structure

- [ Overview](#-overview)
- [ Features](#-features)
- [ Project Structure](#-project-structure)
- [ Getting Started](#-getting-started)
    - [ Prerequisites](#-prerequisites)
    - [ Installation](#-installation)
    - [ Usage](#-usage)
    - [ Testing](#-testing)
- [ Project Roadmap](#-project-roadmap)
- [ License](#-license)
- [ Acknowledgments](#-acknowledgments)

---

## ⚡ Getting Started

### ✅ Prerequisites

- Java 17+
- Docker + Docker Compose
- Maven 3.8+

### 📚 Installation

```bash
# Clone the repo
$ git clone https://github.com/DiegoBustos16/MedicalTestVisor
$ cd MedicalTestVisor

# Build all services (optional if using docker-compose)
$ ./mvnw clean install -DskipTests

# Start infrastructure + services
$ docker-compose up --build
```

### 🚀 Usage

- Access the API Gateway at `http://localhost:8080`
- Swagger UI is available in each microservice via `/swagger-ui.html`
- Keycloak is accessible at `http://localhost:8081`

### 🎓 Testing

```bash
# Run tests for a specific service
$ ./mvnw test -f doctor-microservice
```

---

## 🔄 Project Roadmap

- [x] Microservices structure with Spring Boot
- [x] Security with Keycloak and OAuth2
- [x] Image upload and retrieval with S3
- [ ] Frontend for advanced visualization (coming soon)

---

## 💼 License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/DiegoBustos16/MedicalTestVisor/blob/main/LICENSE) file for details.

---

## 👏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Keycloak](https://www.keycloak.org/)
- [Amazon AWS S3](https://aws.amazon.com/s3/)
- [Docker](https://www.docker.com/)
- [OpenAPI/Swagger](https://swagger.io/)

---

> Made by [Diego Bustos](https://github.com/DiegoBustos16)
