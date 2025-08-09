# Stage 1: Build
FROM node:20-alpine as build
WORKDIR /app
COPY . .

# Set the VITE_SERVER environment variable during build
ARG VITE_SERVER
ENV VITE_SERVER=${VITE_SERVER}

# Debug: Print the environment variable
RUN echo "VITE_SERVER during build: $VITE_SERVER"

RUN npm install && npm run build

# Stage 2: Nginx for serving static files
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html

# Create custom nginx configuration for port 5173
RUN echo 'server { \
    listen 5173; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html index.htm; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|json)$ { \
        expires 1y; \
        add_header Cache-Control "public, immutable"; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 5173
CMD ["nginx", "-g", "daemon off;"]

