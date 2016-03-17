#
# HTRC-Portal Dockerfile
#
# https://github.com/htrc/HTRC-Portal
#

# Base image
FROM htrc/oracle-java7:alpine

# Install dockerize to handle service dependencies
RUN wget https://github.com/jwilder/dockerize/releases/download/v0.0.4/dockerize-linux-amd64-v0.0.4.tar.gz
RUN tar -C /usr/local/bin -xvzf dockerize-linux-amd64-v0.0.4.tar.gz

WORKDIR /opt

ADD htrc-portal-3.2.0-SNAPSHOT.zip /opt/
RUN unzip htrc-portal-3.2.0-SNAPSHOT.zip

ADD portal.sh /opt/
RUN chmod +x /opt/portal.sh

EXPOSE 9000

CMD ["dockerize", "-wait", "tcp://idp:9443","/opt/portal.sh"]
