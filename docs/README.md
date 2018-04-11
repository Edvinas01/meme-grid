# Meme Grid
<p align="center">
  <img src="screenshot.jpeg"/>
</p>

## Building
To build the project, you'll need to have JDK 8 installed. After you've got that ready, go to 
root project dir and run:
```bash
./gradlew build
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
If you have Docker and Docker Compose setup on your machine, you can simply run:
```bash
docker-compose up
```

This will also start the application on port `8080` and also expose PostgreSQL on port 
`5432`.

## Help
You can get more info on available options by running:
```bash
java -jar meme-grid.jar --help
```
