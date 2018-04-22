FROM openjdk:8

WORKDIR /memegrid
ADD . /memegrid

CMD ./gradlew shadowJar && java -jar build/libs/meme-grid.jar -u db:5432/memes
