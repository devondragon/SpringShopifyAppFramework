# Getting Started

To create the docker based DB:

docker run -p 127.0.0.1:3306:3306 --name shopify-test -e MARIADB_ROOT_PASSWORD=shopifytestroot -e MARIADB_DATABASE=shopifytest -e MARIADB_USER=shopifytest -e MARIADB_PASSWORD=shopifytest -d mariadb:latest

To run the app:

gradle bootRun

