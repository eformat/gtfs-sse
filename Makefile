# Image URL to use all building/pushing image targets
REGISTRY ?= quay.io
REPOSITORY ?= $(REGISTRY)/eformat/gtfs-sse

IMG := $(REPOSITORY):latest

# clean compile -Pnative -Dnative-image.xmx=16g
compile:
	mvn clean package -DskipTests -Pnative

# Podman Login
podman-login:
	@podman login -u $(DOCKER_USER) -p $(DOCKER_PASSWORD) $(REGISTRY)

# Build the oci image no compile
podman-build-nocompile:
	podman build --no-cache . -t ${IMG} -f Dockerfile.native

# Build the oci image
podman-build: compile
	podman build . -t ${IMG} -f Dockerfile.native

# Push the oci image
podman-push: podman-build
	podman push ${IMG}

# Push the oci image
podman-push-nocompile: podman-build-nocompile
	podman push ${IMG}

# Just Push the oci image
podman-push-nobuild:
	podman push ${IMG}

podman-run:
	podman-compose -f docker-compose.yaml up -d

podman-stop:
	podman-compose -f docker-compose.yaml down
