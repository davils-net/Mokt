# MIT License
# Copyright 2024 Davils
#
# Permission is hereby granted, free of charge, to any person obtaining
# a copy of this software and associated documentation files (the "Software”),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software.

build-writerside-docker-container:
	echo "Building writerside docker container"
	docker build -t mokt-docs .

install-rustup:
	echo "Installing rustup"
	curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
	rustup default stable

run-writerside-docker-container:
	echo "Running writerside docker container"
	docker run --rm -p 80:80 -d mokt-docs

run-writerside-docker-container-with-compose:
	echo "Running writerside docker container with compose"
	docker compose up -d
