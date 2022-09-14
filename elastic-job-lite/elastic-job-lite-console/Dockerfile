FROM openjdk:8-jre-slim

COPY target/elastic-job-lite-console-2.1.5.tar.gz .

RUN tar -zxvf elastic-job-lite-console-2.1.5.tar.gz

WORKDIR /elastic-job-lite-console-2.1.5

COPY mysql-connector-java-8.0.30.jar lib/

EXPOSE 8899

ENTRYPOINT ["./bin/start.sh"]