install: down up

up:
	@ echo Up service
	@ docker compose up -d --build	
	
down:
	@ echo Down services
	@ docker compose down

sonar-scanner:
	@ mvnw clean verify sonar:sonar \
  		-Dsonar.projectKey=Production \
  		-Dsonar.projectName='Production' \
  		-Dsonar.host.url=http://127.0.0.1:9000 \
  		-Dsonar.token=sqp_b12951f6a83a0b69bb2d0783a39347cbdb1ce8ac
