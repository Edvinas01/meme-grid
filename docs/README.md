<p align="center">
  <a href="https://memes.danevicius.lt" target="_blank">
    <img src="screenshot.jpeg"/>
  </a>
</p>

# Meme Grid [![Build Status](https://travis-ci.org/Edvinas01/meme-grid.svg?branch=master)](https://travis-ci.org/Edvinas01/meme-grid)
The only hub for memes that you'll ever need.

## Building
To build the project, you'll need to have JDK 8 installed. After you've got that ready, go to root 
project dir and run:
```bash
./gradlew shadowJar
``` 

This will create an executable .jar under `libs/build`. 

## Running
Before running the project, you'll have to setup PostgreSQL. Afterwards simply execute the built 
jar:
```bash
java -jar meme-grid.jar
```

This will start the application and bind it to port `8080`.

## Running using Docker
If you have Docker and Docker Compose setup, you can simply run:
```bash
sudo docker-compose -f scripts/docker-compose.yml -p memegrid up
```

This will start the application on port `8080` and expose PostgreSQL on port `5432`.
