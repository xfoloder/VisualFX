EchoTest компилируется на JDK 25 вместе с реальным EchoHistory.java, без Minecraft:
javac -encoding UTF-8 -d verification-classes src/main/java/dev/visualfx/EchoHistory.java verification/echo/EchoTest.java
java -ea -cp verification-classes dev.visualfx.EchoTest
Проверяются ёмкость истории, срок жизни, неизменность опубликованного снимка, смена цели, телепортация, неподвижность, поворот и очистка.
