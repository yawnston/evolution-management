FROM maven:3.8-openjdk-17-slim

RUN su -;
RUN apt update;
RUN apt install sudo -y;
RUN exit;

COPY . /app
WORKDIR /app
RUN mvn install -Dmaven.test.skip;

WORKDIR /app/server
CMD ["mvn", "spring-boot:run"]
