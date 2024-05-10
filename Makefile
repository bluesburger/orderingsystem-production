install: down up

down-all: down down-local sonarqube-down

build:
	@ .\mvnw clean install

up:
	@ echo Up service
	@ docker compose up -d --build
	
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
	