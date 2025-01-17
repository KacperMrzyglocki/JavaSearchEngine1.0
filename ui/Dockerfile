FROM node:18

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install

COPY . .

# Otwórz port, na którym działa aplikacja React w trybie deweloperskim
EXPOSE 3000

CMD ["npm", "start"]
