# ── Stage 1: Build ─────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

# Копируем манифест отдельно для кэширования слоя
COPY package*.json ./
RUN npm ci --prefer-offline

COPY . .
RUN npm run build

# ── Stage 2: Serve with Nginx ───────────────────
FROM nginx:1.27-alpine

# SPA + API proxy конфиг
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Артефакты Vite build
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -q --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
