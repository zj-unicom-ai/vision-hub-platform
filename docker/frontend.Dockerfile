# syntax=docker/dockerfile:1
# Build the Vue 3 console and serve it with nginx (/api is proxied to the web service).

# ---- Build stage ----
FROM node:20-alpine AS build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Runtime stage ----
FROM nginx:1.27-alpine
ENV TZ=Asia/Shanghai
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
