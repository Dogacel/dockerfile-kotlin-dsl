FROM openjdk:21-jdk-slim
WORKDIR /app
# Copy the JAR file into the Docker Image
COPY app.jar /app/app.jar
# Download assets
RUN curl -o assets.zip https://example.com//full_assets.zip
RUN unzip assets.zip -d /app/assets && rm assets.zip
EXPOSE 443/tcp
CMD java -jar app.jar