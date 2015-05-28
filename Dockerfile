#
# HTRC-Portal Dockerfile
#
# https://github.com/htrc/HTRC-Portal
#

# Base image
FROM htrc/oracle-java7

# RUN useradd -ms /bin/bash wso2is

RUN apt-get update

RUN apt-get install -y unzip

# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

WORKDIR /opt

ADD htrc-portal-3.2-SNAPSHOT.zip /opt/
RUN unzip htrc-portal-3.2-SNAPSHOT.zip

# this is required because current maven build change file permission during wso2is repackaging.
RUN chmod +x /opt/htrc-portal-3.2-SNAPSHOT/bin/htrc-portal

#RUN mkdir -p /etc/my_init.d
#ADD wso2is.sh /etc/my_init.d/wso2is.sh
#RUN chmod +x /etc/my_init.d/wso2is.sh

# Make htrc-idpandregistry run as a daemon.
RUN mkdir /etc/service/portal
ADD portal.sh /etc/service/portal/run
RUN chmod +x /etc/service/portal/run

# TODO: We need to figure out how to provide application.conf to run script

EXPOSE 9000

# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]


