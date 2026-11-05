# Receta para que Render arme y arranque el back.
# Tiene dos etapas: la primera compila con el JDK completo; la segunda solo copia el .jar a una imagen
# pequeña (JRE), así la imagen final pesa menos y no lleva Gradle ni el código fuente.

# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:21-jdk AS compilacion
WORKDIR /app

# Primero solo lo de Gradle: si build.gradle no cambia, Docker reutiliza las dependencias descargadas
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null

# Luego el código. Corre las pruebas antes de armar el .jar: si una falla, no se despliega
# y en Render sigue funcionando la versión anterior.
COPY src src
RUN ./gradlew test bootJar --no-daemon

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# No correr como administrador del contenedor
RUN useradd --system --no-create-home uqdelivery
USER uqdelivery

COPY --from=compilacion /app/build/libs/*.jar app.jar

# Render gratis da 512 MB: Java usa máximo el 75 % para no pasarse y que Render no lo reinicie.
# Hora de Colombia para las fechas que guarda el back (LocalDateTime.now()).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+UseSerialGC", "-Xss512k", "-Duser.timezone=America/Bogota", "-jar", "app.jar"]
