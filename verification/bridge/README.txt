Эти изолированные проверки запускают реальный SoundCloudPlayer с минимальными заглушками Config/VisualFX без Minecraft. Заглушки используются только для этой проверки и не входят в JAR.
Компиляция на JDK 21:
  javac -encoding UTF-8 -cp <путь-к-gson.jar> -d <каталог-классов> verification/bridge/VisualFX.java src/main/java/dev/visualfx/SoundCloudPlayer.java verification/bridge/BridgeTest.java
Запуск с -ea и classpath из каталога классов, Gson JAR и src/main/resources:
  java -ea -cp <classpath> dev.visualfx.BridgeTest
В Windows части classpath разделяются точкой с запятой, в Linux/macOS двоеточием. Gson доступен в кэше Gradle после сборки проекта.
Проверяется локальный канал, а не настоящее воспроизведение SoundCloud.

