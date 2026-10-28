FROM nginx:1.27-alpine

# Copy custom reverse proxy configuration into standard Nginx config directory
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

