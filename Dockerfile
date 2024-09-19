#image to run gatling tests
FROM gradle:8.10.1-jdk17 AS build

ARG BASE_URL=http://localhost:5000
ARG API_TOKEN=5e7be6e64dd64c19aa9d385a12314d953b2207a138b1453293f43c2e5ab550be
ARG RAMP_TO=10
ARG RAMP_TO=10
ARG RAMP_TIME=1
ARG CONCURRENT_USERS=20
ARG CONCURRENT_TIME=1

#COPY . /home/gradle/redischeck
RUN git clone https://github.com/yecid-rodriguez-mojix-com/redischeck.git /home/gradle/redischeck

WORKDIR /home/gradle/redischeck

ENV ENV_BASE_URL=$BASE_URL
ENV ENV_API_TOKEN=$API_TOKEN
ENV ENV_RAMP_TO=$RAMP_TO
ENV ENV_RAMP_TO=$RAMP_TO
ENV ENV_RAMP_TIME=$RAMP_TIME
ENV ENV_CONCURRENT_USERS=$CONCURRENT_USERS
ENV ENV_CONCURRENT_TIME=$CONCURRENT_TIME

#RUN gradle gatlingRun --simulation=towbook.LocationSuiteSimulation  -DbaseUrl=$BASE_URL -DapiToken=$API_TOKEN -DrampTo=$RAMP_TO -DrampTime=$RAMP_TIME -DconcurrentUsers=$CONCURRENT_USERS -DconcurrentTime=$CONCURRENT_TIME

#ENTRYPOINT ["/bin/sh","/home/gradle/redischeck/launch.sh"]


# #Final image
# FROM openjdk:17-jdk-slim
#
# COPY --from=build /home/gradle/redischeck/build/reports/gatling /opt/gatling/results
#
# WORKDIR /opt/gatling/results

ENTRYPOINT ["tail", "-f", "/dev/null"]