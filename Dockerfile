#
# HTRC-Portal Dockerfile
#
# https://github.com/htrc/HTRC-Portal
#

# Base image
FROM anapsix/alpine-java:jdk8

MAINTAINER Data to Insight Center <d2i@indiana.edu>

COPY target/universal/stage/ /opt/portal/
WORKDIR /opt/portal/

ADD wait /usr/local/bin/wait

EXPOSE 9000

CMD ["sh", "/usr/local/bin/wait"]
