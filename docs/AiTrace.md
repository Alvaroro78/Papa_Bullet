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

## 2026-10-09 · Módulo P3 (armas + niveles) a partir del reparto (registro retroactivo)

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1 (integrante a cargo de P3)
- **Herramienta:** Claude (Anthropic), modelo configurado `claude-opus-5-5`, a partir del PDF del reparto, sin acceso al proyecto
- **ACTION_TYPE:** AI_GENERATED
- **Tarea:** hacer el punto 3 del reparto: interfaz `Weapon`, Thrower Papa, French Spikes, Oil Slick, explosiones, desbloqueos y los 4 mapas.
- **Archivos generados:** `com/papabullet/entity/weapon/` (`AimDirection`, `Weapon`, `BaseWeapon`, `Projectile`, `Explosion`, `ThrowerPapa`, `FrenchSpikes`, `OilSlick`, `Arsenal`), `resources/levels/nivel0.txt` a `nivel3.txt` y `NOTAS_P3.md`.
- **Nota:** registro retroactivo. Ocurrió antes del checkpoint validado y se agrega al final a pedido del equipo, sin editar lo anterior.
- **Resumen:** se escribió según la estructura del reparto (`com.papabullet`, `Level.isSolidAt`, `Camera.getX`). Los mapas se generaron con un script que valida un solo `P` y un solo `B` y que ningún enemigo quede flotando.
- **Pruebas:** se compiló contra versiones de prueba del motor y de JavaFX; pasaron 20 pruebas simuladas (daño, enfriamiento, alcance de las espinas, charco sobre el piso, cambio y desbloqueo de armas).
- **Limitaciones:** el código de armas **no se integró**: el proyecto real usa el paquete `model` y la interfaz `Weapon` de P2. El integrante pidió después ir paso a paso. Los mapas sí se usaron (ver más abajo).
- **Commit sugerido:** no aplica.

## 2026-10-10 · Análisis y guía del merge P2 → P1 (registro retroactivo)

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1 (integrante a cargo de P3)
- **Herramienta:** Claude (Anthropic), modelo configurado `claude-opus-5-5`, sobre copias (zips) del proyecto que compartió el equipo
- **ACTION_TYPE:** AI_ASSISTED
- **Tarea:** decir dónde habría conflicto al fusionar `origin/p2` (`72f9a03`) en `origin/develop` (`5cec3bb`) y cómo corregirlo.
- **Archivos:** merge de prueba en una copia. Se entregaron `model/Player.java` y `model/InputHandler.java` ya adaptados y una guía en un documento compartido de Claude.
- **Nota:** registro retroactivo. Ocurrió antes del checkpoint validado y se agrega al final a pedido del equipo, sin editar lo anterior.
- **Resumen:**
  - Git marcaba 2 conflictos (`Entity` y `GameWorld`, add/add). Había 4 más que no marcaba: `control/InputHandler` duplicado, `Player` hecho contra la interfaz `GameWorld` de P2, `PlayerSandbox` y la letra `P` creando el `DebugPlayer`.
  - Se dejaron los contratos de P1. A `InputHandler` de P1 se le agregó `pressOrder()`, y a `Player` se le quitó su física propia, porque si no, el motor lo movía dos veces por frame.
  - El zip "que ofrece" estaba en la rama local `p3`; se usó `origin/p2`.
- **Pruebas:** el paquete `model` compiló contra versiones de prueba de JavaFX. Una simulación sin ventana pasó 7 pruebas: la `P` crea el `Player`, queda en el piso, camina 200 px/s, salta, vuelve al piso, apunta con `pressOrder` y se dibuja con la cámara.
- **Limitaciones:** el merge real lo hizo el equipo en GitHub (PR #5, `5e9a2e9`) y no siguió la guía en `GameWorld` (ver la entrada siguiente).
- **Commit sugerido:** `Merge p2 en develop: Player sobre el motor de P1`

## 2026-10-10 · Errores de compilación después del PR #5 (registro retroactivo)

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1 (integrante a cargo de P3)
- **Herramienta:** Claude (Anthropic), modelo configurado `claude-opus-5-5`, sobre copias (zips) del proyecto que compartió el equipo
- **ACTION_TYPE:** AI_ASSISTED
- **Tarea:** revisar los errores que salían después del merge.
- **Archivos:** ninguno modificado en el repositorio. Se indicó cambiar `model/GameWorld.java` y `model/Player.java`.
- **Nota:** registro retroactivo. Ocurrió antes del checkpoint validado y se agrega al final a pedido del equipo, sin editar lo anterior.
- **Resumen:** 4 problemas.
  - `GameWorld` quedó como la interfaz de P2. Se recupera con `git checkout 5cec3bb -- .../model/GameWorld.java`. Este causaba 6 de los 7 errores.
  - Faltaba `Player.selectedSkin`.
  - Faltaba `this.frames = loadFrames(skin);` en el constructor.
  - La recarga del King Papa estaba invertida (`k.isUsed()` sin `!`) y no se marcaba como usado.
- **Pruebas:** con los 4 cambios, el paquete `model` compiló y la simulación anterior volvió a pasar.
- **Limitaciones:** según Git, `GameWorld` vuelve a ser clase en `b5b3a00` (PR #6), `pressOrder` entra en `208600a` y la recarga se corrige en `47f1d6f`.
- **Commit sugerido:** `Corrige el merge de p2: GameWorld de P1 y recarga del King Papa`

## 2026-10-10 · Mapas ajustados al jugador real y Canvas en Game.fxml (registro retroactivo)

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1 (integrante a cargo de P3)
- **Herramienta:** Claude (Anthropic), modelo configurado `claude-opus-5-5`, sobre copias (zips) del proyecto que compartió el equipo
- **ACTION_TYPE:** AI_GENERATED (mapas) · AI_ASSISTED (`Game.fxml`)
- **Tarea:** dejar listos los 4 niveles y averiguar por qué START no hacía nada.
- **Archivos:** `resources/assets/levels/nivel0.txt` a `nivel3.txt` (entregados). Se indicó cambiar `resources/assets/views/Game.fxml`.
- **Nota:** registro retroactivo. Ocurrió antes del checkpoint validado y se agrega al final a pedido del equipo, sin editar lo anterior.
- **Resumen:**
  - Con el salto real del jugador (unas 3 baldosas) algunos enemigos y King Papa no se alcanzaban. Se bajaron plataformas y las zonas `J` se pusieron encima de las repisas.
  - START lanzaba `NullPointerException` en `GameController.java:65`, porque `Game.fxml` todavía tenía el `GridPane` de la plantilla. Se reemplazó por `<Canvas fx:id="canvas" width="800" height="600">` y se agregó `fx:id="root"`.
- **Pruebas:** un script de alcance dio "todo alcanzable" en los 4 mapas. En el motor del proyecto, los 4 cargan con el jugador en el piso, y el jetpack lo deja en la repisa (pies en y = 200).
- **Limitaciones:** según Git, los 4 `.txt` de `bc003e5` (PR #7) son idénticos a los entregados, y el `Canvas` también entra en ese commit. En `47f1d6f` pasan a `.json` sin cambiar ninguna baldosa.
- **Commit sugerido:** `Niveles 0 a 3 y Canvas en Game.fxml`

## 2026-10-10 · Revisión del proyecto contra el reparto y el enunciado

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1 (integrante a cargo de P3)
- **Herramienta:** Claude (Anthropic), modelo configurado `claude-opus-5-5`, sobre copias (zips) del proyecto que compartió el equipo
- **ACTION_TYPE:** AI_ASSISTED
- **Tarea:** decir qué faltaba en todo el proyecto según el reparto y el PDF del juego. Se revisó dos veces: en `0fc6c45` y en `67d58d7` (el checkpoint, con los cambios del equipo).
- **Archivos:** ninguno modificado (solo lectura).
- **Resumen:**
  - Se jugaron los 4 niveles en una simulación sin ventana: cada jefe pasa al siguiente nivel y el último llega a juego completado.
  - Ya corregido por el equipo en `67d58d7`: `mvn javafx:run` (el `mainClass` pasó a `gui.App`) y el nombre del nivel 2.
  - Pendientes:
    - las armas no se conservan entre niveles y Acesi no trae ninguna para recoger, así que en el nivel final solo hay Thrower Papa;
    - no hay desbloqueo progresivo;
    - el jetpack funciona en cualquier parte y el PDF dice que solo en ciertas zonas;
    - desde `ea2ad5c` el mapa del nivel 2 es casi igual al del 3 (11 baldosas distintas);
    - no hay helicópteros en el nivel 1;
    - policías y helicóptero no tienen sprite.
  - Por confirmar con el profesor: qué es el "+150 sec" del King Papa, y la tecla Ñ, que no existe en teclados en inglés.
- **Pruebas:** compilación de `model` y `gui/Hud` contra versiones de prueba de JavaFX, y la simulación de los 4 niveles.
- **Limitaciones:** en la copia que compartió el equipo estaban borrados, sin commit, `.mvn/wrapper/maven-wrapper.jar` y `.properties`. Se avisó que `git add .` los borraría del repositorio. Los pendientes siguen abiertos salvo que el equipo los haya cerrado después: **[COMPLETAR DESDE GIT]**.
- **Commit sugerido:** no aplica.

## 2026-10-10 · Trazabilidad de la sesión del punto 3, el merge y la revisión

- **PARTICIPANT_ID:** Los 4 Fantasticos + 1 (integrante a cargo de P3)
- **Herramienta:** Claude (Anthropic), modelo configurado `claude-opus-5-5`
- **ACTION_TYPE:** DOCUMENTATION
- **Tarea:** añadir a los dos documentos solo lo de esta sesión, dejando el resto igual.
- **Archivos:** `README_TRAZABILIDAD.md`, `AI_TRACE.md`
- **Resumen:**
  - README: hitos 14 a 16 y una nota sobre ellos, el modelo de esta sesión, ejemplos nuevos (depuración, resolución de merge, diseño de niveles, revisión contra el enunciado) e intervenciones humanas.
  - AI_TRACE: estas 6 entradas al final.
  - Los commits se verificaron en el historial de Git del zip.
- **Pruebas:** no aplica.
- **Limitaciones:** las 4 primeras entradas son retroactivas (anteriores al checkpoint). El equipo debe confirmar si los pendientes siguen abiertos.
- **Commit sugerido:** `Docs: trazabilidad del punto 3, el merge P2 → P1 y la revisión del proyecto`
