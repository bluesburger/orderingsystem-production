# Usando a imagem do Maven para compilar o aplicativo
FROM maven:3.8.5-openjdk-17 AS builder

# Defina a variável de ambiente para controlar os testes
ARG SKIP_TESTS=true
ENV SKIP_TESTS=${SKIP_TESTS}

# Definindo o diretório de trabalho no contêiner
WORKDIR /app

# Copiando o arquivo de definição de projeto e os arquivos de código-fonte
COPY pom.xml .
COPY src ./src

# Compilando o aplicativo e gerando o arquivo JAR, com base na variável de ambiente SKIP_TESTS
RUN mvn clean install -DskipTests=${SKIP_TESTS}

# Usando a imagem do Amazon Corretto para executar o aplicativo
FROM amazoncorretto:17-al2-native-jdk

WORKDIR /app

# Copie o JAR gerado a partir da etapa anterior para o contêiner
COPY --from=builder /app/target/orderingsystem-production-0.0.1-SNAPSHOT.jar .

# Expondo a porta que o aplicativo está ouvindo
EXPOSE 9000

# Comando para iniciar o aplicativo
ENTRYPOINT ["java", "-jar", "orderingsystem-production-0.0.1-SNAPSHOT.jar"]