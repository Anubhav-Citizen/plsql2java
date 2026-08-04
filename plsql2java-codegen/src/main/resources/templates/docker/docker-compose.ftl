services:
  app:
    build: .
    image: ${artifactId}:1.0.0
    ports:
      - "8080:8080"
    environment:
      DB_URL: ${r"${DB_URL}"}
      DB_USERNAME: ${r"${DB_USERNAME}"}
      DB_PASSWORD: ${r"${DB_PASSWORD}"}
    env_file:
      - .env
    depends_on:
      db:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ${r"${DB_NAME}"}
      POSTGRES_USER: ${r"${DB_USERNAME}"}
      POSTGRES_PASSWORD: ${r"${DB_PASSWORD}"}
    env_file:
      - .env
    volumes:
      - db-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $${r"${DB_USERNAME}"}"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  db-data:
