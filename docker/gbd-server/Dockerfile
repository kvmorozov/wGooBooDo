FROM adoptopenjdk/openjdk14:alpine-slim

LABEL maintainer="kmorozov"
LABEL name="gbd-server"
LABEL version="latest"

RUN echo '@edge http://dl-cdn.alpinelinux.org/alpine/edge/community' >> /etc/apk/repositories && \
    apk -U upgrade && \
    apk -v add tor@edge curl && \
    chmod 700 /var/lib/tor && \
    rm -rf /var/cache/apk/* && \
    tor --version
COPY torrc /etc/tor/
COPY torrc-defaults /etc/tor/

HEALTHCHECK --timeout=10s --start-period=60s \
    CMD curl --fail --socks5-hostname localhost:9150 -I -L 'https://www.facebookcorewwwi.onion/' || exit 1
	
RUN mkdir -p /apps
COPY ru.kmorozov.library.data.server-2.2-b2.jar /apps/gbd-server.jar
COPY gbd-entrypoint.sh /apps/gbd-entrypoint.sh
RUN chmod +x /apps/gbd-entrypoint.sh
COPY application.properties /apps/application.properties

RUN mkdir -p /logs

EXPOSE 9000/tcp

RUN mkdir -p /apps/data

WORKDIR /apps

ENTRYPOINT /apps/gbd-entrypoint.sh