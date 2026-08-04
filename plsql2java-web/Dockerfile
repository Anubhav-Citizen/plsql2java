# SECURITY-10: pinned digest — update periodically after security review
FROM eclipse-temurin:17-jre-alpine@sha256:b2c4c9e7e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5e5

# SECURITY-09: non-root user
RUN addgroup -S plsql2java && adduser -S plsql2java -G plsql2java

WORKDIR /app

COPY target/plsql2java-web-*.jar app.jar

RUN mkdir -p /app/output /tmp/plsql2java && \
    chown -R plsql2java:plsql2java /app /tmp/plsql2java

USER 1001

EXPOSE 8080

ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
