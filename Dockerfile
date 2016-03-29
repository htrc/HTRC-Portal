#
# HTRC-Portal Dockerfile
#
# https://github.com/htrc/HTRC-Portal
#

# Base image
FROM htrc/java7:alpine

# Install dockerize to handle service dependencies
RUN wget https://github.com/jwilder/dockerize/releases/download/v0.2.0/dockerize-linux-amd64-v0.2.0.tar.gz && \
    tar -C /usr/local/bin -xzvf dockerize-linux-amd64-v0.2.0.tar.gz

COPY target/universal/stage/ /opt/portal/
WORKDIR /opt/portal/

EXPOSE 9000

CMD ["dockerize", "-timeout", "120s", "-wait", "$IDP_EP", "-wait", "$MYSQL_EP", "./bin/htrc-portal"]
