# AI_TRACE — Papa_Bullet

EVENT_ID: AI_GAME_JAM · TEAM_ID: Los 4 fantasticos + 1

Registro incremental del uso de IA: **solo se agregan entradas al final; las anteriores no se editan ni se borran.**
Este archivo empieza en el checkpoint validado **"Fondos personalizados x nivel y corrección de nombres"**.
Lo anterior a ese punto está en la sección "Trazabilidad del desarrollo" del README y en el historial de Git;
aquí no se reconstruye.

Formato de cada entrada: fecha · participante · herramienta · tipo · tarea · archivos · resumen · pruebas · limitaciones · commit.

---

## 2026-10-10 · Contrarreloj y calentamiento del arma

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1
- **Herramienta:** Claude (Anthropic), agente sobre una copia del proyecto
- **ACTION_TYPE:** AI_GENERATED
- **Tarea:** contrarreloj de 4 minutos por nivel; si el arma se sobrecalienta, el tiempo baja más rápido.
- **Archivos:** `src/main/java/model/Player.java`, `src/main/java/model/GameWorld.java`, `src/main/java/gui/Hud.java`, `src/test/java/model/PlayerTest.java`
- **Resumen:**
  - Cada disparo calienta el arma +0.25 y se enfría 0.25 por segundo. Si llega a 1 queda sobrecalentada hasta volver a 0.
  - El reloj (`GameWorld.LEVEL_TIME` = 240 s) se reinicia al cargar o reiniciar el nivel. Con el arma sobrecalentada corre ×3 (`OVERHEAT_TIME_RATE`). En 0 el jugador muere y se reinicia el nivel.
  - HUD: el reloj va arriba al centro y se pone rojo en los últimos 30 s o con el arma sobrecalentada. Debajo de las armas hay una barra de calor y el aviso "¡SOBRECALENTADA! RELOJ x3".
- **Supuestos (no los especificó el equipo):**
  - Con el arma sobrecalentada se puede seguir disparando; el castigo es solo el reloj.
  - El calor es igual para las 3 armas.
- **Pruebas:**
  - 3 tests nuevos en `PlayerTest`: se sobrecalienta y se enfría, el reloj corre ×3 y al acabarse el tiempo muere y el nivel vuelve a 4:00.
  - Todos los tests pasan (10 + 4 + 4 + 4 + 3), pero contra una imitación de JavaFX y JUnit. **Falta confirmarlos con `mvn test` en JavaFX real.**
- **Limitaciones:** el equilibrio (+0.25 por disparo y −0.25 por segundo) no se ha probado jugando. Con la Oil Slick, por su cadencia, casi nunca se sobrecalienta.
- **Commit sugerido:** `Contrarreloj de 4 min por nivel y calentamiento del arma`

## 2026-10-10 · README de trazabilidad y creación de AI_TRACE.md

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1
- **Herramienta:** Claude (Anthropic)
- **ACTION_TYPE:** DOCUMENTATION
- **Tarea:** redactar la sección "Trazabilidad del desarrollo" del README y crear este archivo.
- **Archivos:** `README_TRAZABILIDAD.md` (se pega en `README.md`), `AI_TRACE.md`
- **Resumen:** hitos en orden, herramientas de IA, ejemplos, intervenciones humanas y pendientes, usando solo lo que consta en la conversación. Fechas y commits marcados como [COMPLETAR DESDE GIT].
- **Pruebas:** no aplica.
- **Limitaciones:** el equipo debe completar las fechas y hashes desde Git y las herramientas de IA usadas por los demás integrantes.
- **Commit sugerido:** `Docs: trazabilidad del desarrollo y AI_TRACE`
