Проверка запускает реальные ColorStyle.java и ColorTarget.java с минимальной заглушкой Config без Minecraft. Заглушка не включается в установочный JAR. Требуются JDK 25 и Gson из зависимостей проекта.

javac -encoding UTF-8 -cp <путь-к-gson.jar> -d verification-classes verification/colors/dev/visualfx/*.java src/main/java/dev/visualfx/ColorStyle.java src/main/java/dev/visualfx/ColorTarget.java
java -ea -cp <каталог-классов-и-gson.jar> dev.visualfx.ColorTest

В Windows classpath разделяется точкой с запятой, в Linux/macOS двоеточием. Проверяются HEX, RGB, градиент, JSON и независимость цветовых профилей. Работа ползунков через интерфейс этим тестом не проверяется.
