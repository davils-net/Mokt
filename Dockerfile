# MIT License
# Copyright 2024 Davils
#
# Permission is hereby granted, free of charge, to any person obtaining
# a copy of this software and associated documentation files (the "Software”),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software.

FROM jetbrains/writerside-builder:243.22562 AS builder

ARG INSTANCE=docs/m

RUN mkdir /docs
WORKDIR /docs
ADD docs ./docs

RUN export DISPLAY=:99 && Xvfb :99 & /opt/builder/bin/idea.sh helpbuilderinspect --source-dir /docs --product $INSTANCE --runner other --output-dir /docs/public

WORKDIR /docs/public
RUN unzip -O UTF-8 webHelpM2-all.zip -d /docs/public/unzipped-artifact

FROM caddy:latest AS server

COPY --from=builder /docs/public/unzipped-artifact/ /usr/share/caddy/
EXPOSE 80
