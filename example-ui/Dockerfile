FROM node:16.18-buster as build

COPY . /app
WORKDIR /app

RUN npm install;
RUN npm run build;


FROM nginx:stable-alpine as prod

COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
