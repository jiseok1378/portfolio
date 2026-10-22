# syntax=docker/dockerfile:1.4

# 1단계: 화면 빌드 (Vue3 + Quasar)
FROM node:24-alpine AS front
WORKDIR /app
COPY content ./content
COPY frontend/package.json frontend/package-lock.json ./frontend/
RUN cd frontend && npm ci --ignore-scripts
COPY frontend ./frontend
RUN cd frontend && npx quasar prepare && npx quasar build

# 2단계: 서버 빌드 (빌드된 화면을 정적 파일로 함께 넣는다)
FROM gradle:9-jdk17-alpine AS build
WORKDIR /app
COPY content ./content
COPY backend/build.gradle backend/settings.gradle ./backend/
COPY backend/src ./backend/src
COPY --from=front /app/frontend/dist/spa ./backend/src/main/resources/static
RUN cd backend && gradle bootJar -x test --no-daemon

# 3단계: 실행
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/backend/build/libs/*.jar app.jar
USER app
# Render 무료 인스턴스(메모리가 작음)에 맞춰 힙을 컨테이너 메모리 비율로 잡는다
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
