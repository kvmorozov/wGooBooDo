FROM alpine:3.12

LABEL maintainer="kmorozov"
LABEL name="tor-socks-proxy"
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

EXPOSE 53/udp 9150/tcp 9151/tcp

CMD ["/usr/bin/tor", "-f", "/etc/tor/torrc"]