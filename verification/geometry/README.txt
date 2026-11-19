Проверка использует реальные Geometry.java, Effects.java, ExtraEffects.java и HitEffects.java из src/main/java. Config, Matrix4f и VertexConsumer заменены минимальными заглушками для изолированной проверки математических расчётов без Minecraft/OpenGL. Заглушки не включаются в установочный JAR.

Из корня проекта на JDK 25:
javac -encoding UTF-8 -d verification-classes verification/geometry/dev/visualfx/*.java verification/geometry/org/joml/*.java verification/geometry/com/mojang/blaze3d/vertex/*.java src/main/java/dev/visualfx/Geometry.java src/main/java/dev/visualfx/Effects.java src/main/java/dev/visualfx/ExtraEffects.java src/main/java/dev/visualfx/HitEffects.java
java -cp verification-classes dev.visualfx.GeometryTest

Проверяются конечность и диапазон координат, предел 30000 вершин на эффект за кадр, число режимов, различие последовательностей вершин, уменьшение или сохранение бюджета ESP при оптимизации. Эти проверки не подтверждают визуальное качество, OpenGL-отрисовку и срабатывание атак в игре.
