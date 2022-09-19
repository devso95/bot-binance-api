mvnw package -Dspring.profiles.active=dev && java -jar -Dspring.profiles.active=dev target/future.bot-0.0.1-SNAPSHOT.jar
docker build -t strategy .
aws ecr get-login-password --region ap-southeast-1 | docker login --username AWS --password-stdin 540678088154.dkr.ecr.ap-southeast-1.amazonaws.com
docker tag strategy:latest 540678088154.dkr.ecr.ap-southeast-1.amazonaws.com/strategy:latest
docker push 540678088154.dkr.ecr.ap-southeast-1.amazonaws.com/strategy:latest