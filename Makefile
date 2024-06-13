install: down up

down-all: down down-local sonarqube-down

build-image:
	@ docker compose -f docker-compose-local.yml build application
	@ docker rmi 637423186279.dkr.ecr.us-east-1.amazonaws.com/ordering-system-prod:latest
	@ docker tag ordering-system-prod:latest 637423186279.dkr.ecr.us-east-1.amazonaws.com/ordering-system-prod:latest

build:
	@ .\mvnw clean install -Ppackage

build-image-local:
	@ docker build -f .\Dockerfile.local -t ordering-system-prod:latest .
	@ docker rmi -f 637423186279.dkr.ecr.us-east-1.amazonaws.com/ordering-system-prod:latest
	@ docker tag ordering-system-prod:latest 637423186279.dkr.ecr.us-east-1.amazonaws.com/ordering-system-prod:latest

up:
	@ echo Up service
	@ docker compose up -d --build
	
publish-ecr:
	@ docker push 637423186279.dkr.ecr.us-east-1.amazonaws.com/ordering-system-prod:latest

up-local:
	@ echo Up service
	@ docker compose -f docker-compose-local.yml up -d	
	
up-local-app:
	@ echo Up service
	@ docker compose -f docker-compose-local.yml up -d application
	
down:
	@ echo Down services
	@ docker compose down --volumes
	
down-local:
	@ echo Down services
	@ docker compose -f docker-compose-local.yml down --volumes --remove-orphans
	
down-local-app:
	@ echo Down application container
	@ docker compose -f docker-compose-local.yml down application --volumes --remove-orphans

sonarqube-up:
	@ docker compose -f sonarqube.yml up -d

sonarqube-down:
	@ docker compose -f sonarqube.yml down --volumes

sonarqube-publish:
	@ .\mvnw sonar:sonar
	
sonarqube-analyze: build sonarqube-publish

send-message-to-queue:
	docker compose exec localstack awslocal sqs send-message \
		--queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/${queue-name} \
		--message-group-id "${queue-name}" \
		--message-deduplication-id "${uuid}-${status}" \
		--message-body "{ \"orderId\": \"${uuid}\", \"status\": \"${status}\" }"
		
send-message-to-queue-order-ordered-event-success:
	make send-message-to-queue queue-name=order-ordered-event.fifo uuid=${uuid} status=RESERVED

send-message-to-queue-order-ordered-event-failed:
	make send-message-to-queue queue-name=order-ordered-event.fifo uuid=${uuid} status=FAILED


send-message-to-queue-bill-performed-event-success:
	make send-message-to-queue queue-name=bill-performed-event.fifo uuid=${uuid} status=PAID

send-message-to-queue-bill-performed-event-failed:
	make send-message-to-queue queue-name=bill-performed-event.fifo uuid=${uuid} status=FAILED


send-message-to-queue-invoice-issued-event-success:
	make send-message-to-queue queue-name=invoice-issued-event.fifo uuid=${uuid} status=INVOICE_ISSUED

send-message-to-queue-invoice-issued-event-failed:
	make send-message-to-queue queue-name=invoice-issued-event.fifo uuid=${uuid} status=FAILED


send-message-to-queue-order-scheduled-event-success:
	make send-message-to-queue queue-name=order-scheduled-event.fifo uuid=${uuid} status=SCHEDULED

send-message-to-queue-order-scheduled-event-failed:
	make send-message-to-queue queue-name=order-scheduled-event.fifo uuid=${uuid} status=FAILED