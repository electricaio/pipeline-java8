FROM openjdk:8-alpine

# Create user
RUN adduser --disabled-password --gecos '' --shell /bin/sh electrica

# Create and grant log directory
RUN mkdir -p /var/log/electrica && chown electrica:electrica -R /var/log/electrica

# Set workdir
WORKDIR /home/electrica

# Copy and grant launcher
ADD pipeline-launcher.zip /home/electrica
RUN unzip pipeline-launcher.zip && \
  mv $(find . -type d -iname "electrica-*")/* ./ && \
  chmod 755 bin/electrica-pipeline-java8-launcher && \
  chown electrica:electrica bin/electrica-pipeline-java8-launcher

# Change user
USER electrica

ENTRYPOINT ["./bin/electrica-pipeline-java8-launcher"]
