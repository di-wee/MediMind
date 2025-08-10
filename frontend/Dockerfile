# Stage 1: Build
FROM node:20-alpine as build
WORKDIR /app

# Clear npm cache and disable caching
RUN npm cache clean --force

COPY . .

# Set the VITE_SERVER environment variable during build
ARG VITE_SERVER
ENV VITE_SERVER=${VITE_SERVER}

# Debug: Print the environment variable
RUN echo "VITE_SERVER during build: $VITE_SERVER"

# Install dependencies without cache and build
RUN npm install --no-cache && npm run build

# Stage 2: Nginx for serving static files
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html

# Create custom nginx configuration for port 5173 with cache-busting headers
RUN echo 'server { \
    listen 5173; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html index.htm; \
    location / { \
        try_files $uri $uri/ /index.html; \
        add_header Cache-Control "no-cache, no-store, must-revalidate"; \
        add_header Pragma "no-cache"; \
        add_header Expires "0"; \
    } \
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|json)$ { \
        expires 0; \
        add_header Cache-Control "no-cache, no-store, must-revalidate"; \
        add_header Pragma "no-cache"; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 5173
CMD ["nginx", "-g", "daemon off;"]

