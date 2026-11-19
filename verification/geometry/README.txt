Проверки используют реальные классы геометрии и минимальные заглушки Config/Matrix4f/VertexConsumer. Заглушки не входят в JAR; запуск не проверяет Minecraft/OpenGL.

JDK 25, из корня проекта:
javac -encoding UTF-8 -d verification-classes verification/geometry/dev/visualfx/*.java verification/geometry/org/joml/*.java verification/geometry/com/mojang/blaze3d/vertex/*.java src/main/java/dev/visualfx/Geometry.java src/main/java/dev/visualfx/Effects.java src/main/java/dev/visualfx/ExtraEffects.java src/main/java/dev/visualfx/HitEffects.java src/main/java/dev/visualfx/ColorStyle.java src/main/java/dev/visualfx/EchoHistory.java src/main/java/dev/visualfx/EchoVisual.java
java -cp verification-classes dev.visualfx.GeometryTest
java -cp verification-classes dev.visualfx.EchoGeometryTest

В 1.16.0 выполнялась новая проверка EchoGeometryTest: 1344 сочетания настроек на демонстрационной истории из 24 точек, конечность координат, бюджет <=4000 вершин и восстановление состояния Geometry. Для истории из 32 точек верхний бюджет при восьми силуэтах — 3408 вершин (142 линии по 24 вершины). Полная игровая отрисовка этими проверками не подтверждается.
