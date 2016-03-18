#
# HTRC-Portal Dockerfile
#
# https://github.com/htrc/HTRC-Portal
#

# Base image
FROM htrc/oracle-java7:alpine

# Install dockerize to handle service dependencies
RUN wget https://github.com/jwilder/dockerize/releases/download/v0.2.0/dockerize-linux-amd64-v0.2.0.tar.gz
RUN tar -C /usr/local/bin -xzvf dockerize-linux-amd64-v0.2.0.tar.gz

WORKDIR /opt

ADD htrc-portal-3.2.0-SNAPSHOT.zip /opt/
RUN unzip htrc-portal-3.2.0-SNAPSHOT.zip

ADD portal.sh /opt/
RUN chmod +x /opt/portal.sh

EXPOSE 9000

CMD ["dockerize", "-timeout", "120s", "-wait", "tcp://idp:9443", "-wait", "tcp://mysql:3306", "/opt/portal.sh"]
