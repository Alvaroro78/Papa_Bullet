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

## 2026-10-10 · Revisión del código del equipo: el contrarreloj de Claude no se integra

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1
- **Herramienta:** Claude (Anthropic), sobre el zip del repositorio que compartió el equipo
- **ACTION_TYPE:** AI_ASSISTED
- **Tarea:** revisar el estado real del proyecto antes de explicar las clases.
- **Archivos:** ninguno modificado (solo lectura).
- **Resumen:**
  - Al comparar el zip con la copia del asistente se vio que el equipo ya tenía su propio sistema en `Player`, `Weapon`, `KingPapa` y `Hud`:
    - **vida = tiempo**: un contador de 300 s que baja 1 s por segundo y al que los golpes le quitan segundos;
    - **calor por arma** (`Weapon.getHeat`: Thrower 0.15, Spikes 0.2, Oil 0.3; enfría 0.12/s), que hace bajar el contador hasta ×4 (1 + 3·calor²);
    - el **King Papa como carrito de cholado**, que da +150 s y deja el calor en 0.
  - Se recomendó **no copiar** los archivos de la entrada "Contrarreloj y calentamiento del arma" (`Player`, `GameWorld`, `Hud`, `PlayerTest`), porque reemplazarían el sistema del equipo. Esa entrada queda como registro de lo que se generó, **no de lo que está en el juego**.
  - También se vio que `PlaceholderEntity` ya no lo usa ninguna clase y que `EntityRegistry` crea el `Boss` real. Con eso se descarta que el problema de cómo se ven los jefes venga del jefe de relleno.
- **Pruebas:** no aplica.
- **Limitaciones:**
  - No se sabe qué herramienta usó el integrante que hizo el sistema de vida = tiempo: **[COMPLETAR POR EL EQUIPO]**.
  - Hay que verificar en Git qué versión contiene el commit `ac5b802`.
  - La conversación decía 4:00 por nivel y el código tiene 300 s (5:00); hay que confirmar el valor final.
- **Commit sugerido:** no aplica (no hay cambios de código).

## 2026-10-10 · Explicación de las clases y de la HashTable

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1
- **Herramienta:** Claude (Anthropic)
- **ACTION_TYPE:** DOCUMENTATION
- **Tarea:** explicar en términos sencillos qué hace cada clase y cuál es su rol en el juego, y cómo funciona la `HashTable`, para la exposición del equipo.
- **Archivos:** ninguno modificado. La explicación quedó en el chat.
- **Resumen:**
  - Las 47 clases quedaron explicadas por grupos: arranque y pantallas, motor, niveles, jugador, armas y enemigos.
  - La `HashTable` se explicó con la analogía de un mueble de lockers: `hashCode mod capacidad`, colisiones resueltas con listas enlazadas y duplicar la capacidad al pasar de 0.75.
  - Se dieron sus 4 usos reales en el juego: `Sprites`, `EntityRegistry`, `InputHandler` y `JsonReader`.
- **Pruebas:** no aplica.
- **Limitaciones:** la explicación describe el código del zip revisado; si el código cambia, hay que actualizarla.
- **Commit sugerido:** no aplica.

## 2026-10-10 · Actualización de README de trazabilidad y AI_TRACE

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1
- **Herramienta:** Claude (Anthropic)
- **ACTION_TYPE:** DOCUMENTATION
- **Tarea:** poner al día ambos documentos con lo último de la conversación, partiendo de la versión que editó el equipo (que ya tenía hashes de commits).
- **Archivos:** `README_TRAZABILIDAD.md`, `AI_TRACE.md` (solo se agregaron entradas al final; las anteriores quedan intactas)
- **Resumen:**
  - El hito 12 ahora describe el sistema de vida = tiempo que sí está en el código, con una nota de que el contrarreloj de Claude no se integró.
  - Se volvió a poner el hito del jugador (P2) y se corrigió la numeración.
  - El modelo de esta sesión se registró como `claude-opus-5-5`.
  - Se agregaron como pendientes la diferencia 4:00 / 5:00 y la clase sin uso `PlaceholderEntity`.
- **Pruebas:** no aplica.
- **Limitaciones:** el equipo debe confirmar con qué modelo o herramienta se hizo cada parte en otras sesiones.
- **Commit sugerido:** `Docs: actualiza trazabilidad y AI_TRACE tras revisar el código del equipo`

## 2026-10-10 · Guía para quitar la funcionalidad de Skins

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1
- **Herramienta:** Claude (Anthropic), modelo Claude Sonnet 5.5, sobre el zip del repositorio que compartió el equipo
- **ACTION_TYPE:** AI_ASSISTED
- **Tarea:** el equipo pidió quitar la funcionalidad de skins y un paso a paso para hacerlo.
- **Archivos:** el asistente **no modificó ninguno** (solo leyó una copia extraída del zip). Los archivos que se indicó cambiar al equipo son:
  - borrar `src/main/java/control/SkinController.java` y `src/main/resources/assets/views/Skins.fxml`;
  - `src/main/java/gui/App.java`: quitar `showSkins()`;
  - `src/main/resources/assets/views/MainMenu.fxml`: quitar el botón SKINS (y revisar las cuerdas `rope` que quedan sueltas);
  - `src/main/java/control/MainMenuController.java`: quitar `onSkinsClicked()` y dibujar al personaje fijo con `PJ_1.png`, sin tono;
  - `src/main/java/model/Player.java`: quitar `selectedSkin`, `selectedHue`, el campo `hue` y su efecto `ColorAdjust` en `drawBody`; el constructor vacío pasa a `this("PJ")`.
- **Resumen:**
  - Se buscó "skin" en todo el proyecto y se encontraron 5 puntos de contacto: la pantalla (`SkinController` y `Skins.fxml`), el botón del menú (`MainMenu.fxml` y `MainMenuController`), `App.showSkins()`, las variables estáticas de `Player` y la escena del menú, que dibuja al personaje con el tono elegido.
  - Se indicó dejar los sprites `PJ_*.png`, porque son el personaje, y el campo `skin` y el constructor `Player(String)`, porque `loadFrames` y `currentImage` los usan.
  - Se indicó correr `./mvnw clean compile`, porque `target/classes` conserva copias de `Skins.fxml` y `SkinController.class`.
  - **Hallazgo:** `Skins.fxml` apuntaba a `control.SkinsController`, pero la clase se llama `SkinController`; la pantalla de Skins probablemente no abría. Al borrar ambos archivos el problema desaparece.
  - Primero se dio una guía larga; ante un fallo de visualización del equipo se repitió en versión corta. El contenido es el mismo.
- **Pruebas:** no se compiló ni se ejecutó nada. Se recomendó al equipo probar que el menú abra, que START funcione y que el personaje se vea con su color original en el menú y en el juego.
- **Limitaciones:**
  - Está por confirmar que el equipo aplicó los cambios y cuál es el commit: **[COMPLETAR DESDE GIT]**.
  - Los textos anteriores de la trazabilidad que mencionan la pantalla de Skins describen el estado previo del proyecto y no se editaron.
- **Commit sugerido:** `Quita la funcionalidad de Skins`

## 2026-10-10 · Actualización de trazabilidad por el cambio de Skins

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1
- **Herramienta:** Claude (Anthropic), modelo Claude Sonnet 5.5
- **ACTION_TYPE:** DOCUMENTATION
- **Tarea:** añadir a los dos documentos solo lo ocurrido en la sesión de Skins, dejando el resto igual.
- **Archivos:** `README_TRAZABILIDAD.md`, `AI_TRACE.md`
- **Resumen:**
  - README: hito 13 (se quita la funcionalidad de Skins), un ejemplo de "Retirada de una funcionalidad", la decisión del equipo en "Intervenciones humanas" y un pendiente sobre las menciones antiguas a Skins.
  - AI_TRACE: dos entradas nuevas al final. Las anteriores no se tocaron.
- **Pruebas:** no aplica.
- **Limitaciones:** el commit del hito 13 queda como **[COMPLETAR DESDE GIT]**. El modelo de esta sesión es Claude Sonnet 5.5; el README dice `claude-sonnet5.5` y una entrada previa dice `claude-opus-5-5`, y el equipo debe confirmar cuál corresponde a cada sesión.
- **Commit sugerido:** `Docs: trazabilidad del cambio de Skins`
