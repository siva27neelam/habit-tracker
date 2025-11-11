docker build -t habit-tracker:latest .
docker run -p 8080:8080 habit-tracker:latest
docker login
docker tag habit-tracker:latest siva27neelam/habit-tracker:latest
docker push siva27neelam/habit-tracker:latest

