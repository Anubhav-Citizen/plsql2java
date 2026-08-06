spring:
  application:
    name: ${artifactId}
  datasource:
    url: ${r"${DB_URL}"}
    username: ${r"${DB_USERNAME}"}
    password: ${r"${DB_PASSWORD}"}
    driver-class-name: ${dbDriver}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false

server:
  port: 8080

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    root: INFO
    ${basePackage}: DEBUG
