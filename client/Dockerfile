# ── client/Dockerfile ──
FROM node:20-alpine

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install

COPY . .

# Inside Docker, the backend is the "backend" service on port 8080
ENV VITE_API_BASE_URL=http://backend:8080

CMD ["npm", "run", "dev", "--", "--host"]
