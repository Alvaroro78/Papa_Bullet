## Trazabilidad del desarrollo

> Sección para el README de **Papa_Bullet** — equipo **Los 4 fantasticos + 1** — evento **AI_GAME_JAM**.
> Escrita solo con información verificada en la conversación con el asistente de IA. Fechas, commits y
> hashes no se inventan: donde faltan dice **[COMPLETAR DESDE GIT]**.

### Punto de partida

- Inicio oficial de la ventana del evento: **9/10/2026**.
- Proyecto Maven en **Java + JavaFX** (FXML + Canvas). Paquetes: `control`, `gui`, `model` y `structures`.
- Al empezar ya existía un motor base en `model` hecho por el equipo: `GameWorld`, `Collision`, `Camera`, `Entity`,
  `EntityRegistry`, `LevelLoader`, `InputHandler` y `GameLoop`.
- El trabajo se repartió entre 5 personas (P1 a P5) a partir del enunciado en PDF. A uno de los integrantes
  le tocó la carga más baja.
- Estado del repositorio al empezar: **Vaicio**.

### Hitos principales, en orden
 
| # | Hito | Commit / fecha |
|---|------|----------------|
| 1 | Vistas: menú principal con tablas de madera y paisaje, vista de juego con un Canvas donde viven los niveles y menú de pausa con el juego visible detrás | N/A |
| 2 | Reparto del trabajo en 5 módulos (P1 a P5) con contratos entre ellos (`Weapon`, `Entity`, `Overlay`) | N/A |
| 3 | Jugador (P2): apuntar en 8 direcciones, agacharse, saltar y bajar de plataformas, cambiar de arma, recargar con el King Papa, jetpack y poses del sprite | N/A|
| 4 | Pasada de afinado: se corrigió que el King Papa nunca recargaba y se listó lo que faltaba | 47f1d6f |
| 5 | `HashTable` propia en `structures`, niveles en `.json` con lector JSON propio, y menús con el mismo estilo y colores de buen contraste | 47f1d6f |
| 6 | Armas (Thrower Papa, French Spikes, Oil Slick) que se desbloquean al recogerlas, enemigos (3 policías y helicóptero), 4 jefes, HUD, tutorial en el nivel 0 y pantalla de Skins | 47f1d6f |
| 7 | Correcciones: nombre del nivel legible en el HUD (CSS) y constructor de `Level` que no coincidía con un `LevelLoader` desactualizado | ea2ad5c |
| 8 | Jetpack sin zonas: funciona en cualquier parte del nivel | 7826e93 |
| 9 | Cadencia de disparo por arma: Thrower 0.6 s, Spikes 0.9 s y Oil 1.4 s. Con K sostenida dispara a ese ritmo | 7826e93 |
| 10 | Fondo y terreno distintos por nivel (`Theme`): selva, desierto, ciudad de noche y volcán | 1f05dcc |
| 11 | Nombres corregidos: el nivel 0 pasa a **UVAVALLE** y el nivel 2 vuelve a **JABERUMBA**. **Checkpoint validado:** "Fondos personalizados x nivel y corrección de nombres" | 1f05dcc |
| 12 | **Vida = tiempo** y calentamiento de la máquina, hecho por el equipo. La vida es un contador de 300 s que baja solo; los golpes quitan segundos y cada disparo calienta la máquina (Thrower +0.15, Spikes +0.2, Oil +0.3; se enfría 0.12 por segundo). Con más calor el contador baja más rápido, hasta ×4 con la máquina al rojo. El King Papa pasa a ser un **carrito de cholado** que devuelve +150 s y enfría la máquina. | ac5b802 |
| 13 | **Se quita la funcionalidad de Skins.** Se elimina la pantalla de Skins (`SkinController` y `Skins.fxml`), el botón SKINS del menú principal y el cambio de tono del jugador (`Player.selectedSkin` y `Player.selectedHue`). El personaje queda fijo con los sprites `PJ_*`. El asistente revisó el proyecto y guió los pasos; los cambios en el código los aplica el equipo. | 4c0ce91 |
| 14 | **Merge del jugador (P2) en `develop` (P1).** Git marcaba conflicto en `Entity` y `GameWorld`, y había 4 choques más que no marcaba: dos `InputHandler`, un `Player` hecho contra la interfaz de P2 (que además el motor movería dos veces por frame), el `PlayerSandbox` y la letra `P` que seguía creando el `DebugPlayer`. Se dejaron los contratos de P1 y se adaptó `Player` | 5e9a2e9 (PR #5) |
| 15 | **Corrección del merge.** El PR #5 quedó con la `GameWorld` de P2 (interfaz) y no compilaba. Se recuperó la clase de P1 y se completó `InputHandler` con `pressOrder` | b5b3a00 (PR #6), 208600a |
| 16 | **Los 4 mapas** (`nivel0` a `nivel3`), con todo alcanzable con un salto de 3 baldosas, y el **`Canvas` en `Game.fxml`**: sin él, START lanzaba `NullPointerException` y el juego no abría | bc003e5 (PR #7) |
 
> **Sobre el hito 12.** El asistente también generó su propia versión: reloj aparte de 4:00, calor fijo de +0.25 y
> reloj ×3 con el arma sobrecalentada. **Esa versión no quedó en el código**, porque el repositorio ya tenía la del
> equipo, que es la que se describe arriba. Está registrada en `AI_TRACE.md` como generada y no integrada.

> **Sobre los hitos 14 a 16.** Se agregaron después: por fecha van entre el hito 3 y el 4 (9 y 10 de octubre,
> antes del checkpoint). El asistente hizo el merge y las pruebas en copias del proyecto y dio el paso a paso; los
> commits los hizo el equipo. El paquete de armas que el asistente generó primero para P3 (`com.papabullet.entity.weapon`)
> **no se integró**: el proyecto real usa el paquete `model` y la interfaz `Weapon` de P2. Las armas del juego son las del hito 6.
### Herramientas de IA utilizadas

- **Claude (Anthropic)**, en sesiones de agente con acceso a una **copia** del proyecto. El modelo configurado es
  `claude-sonnet5.5`. El asistente no tenía acceso al repositorio oficial, así que no hizo commits ni push: entregó
  los archivos y el equipo los revisó, vio que podía integrar y los subió.

- En la sesión del punto 3 (armas + niveles), el merge P2 → P1 y la revisión del proyecto, el modelo configurado fue
  `claude-sonnet5.5`. En esa sesión también se usó un documento compartido de Claude con la guía del merge
  ("Papa Bullet — Merge P2 → P1 (develop)").

- Otras herramientas de IA usadas por los demás integrantes: **Gemini**.

### Ejemplos de tareas donde la IA fue relevante
 
- **Diseño de vistas:** el FXML y el CSS de los menús de inicio, pausa y Skins, y el fondo con parallax dibujado con figuras, sin imágenes.
- **Implementación:** el jugador completo (P2), las armas y sus proyectiles, los enemigos y jefes, la `HashTable`, el lector JSON, el HUD y el `Theme` por nivel.
- **Depuración:**
  - el King Papa no recargaba porque la condición estaba invertida y nunca se marcaba como usado;
  - el jetpack no volaba porque dependía de zonas invisibles;
  - el texto del nivel no se leía porque el contorno se dibujaba encima del relleno;
  - el aceite no le pegaba a un policía que caminaba;
  - un error de compilación por un `LevelLoader` desactualizado.
  - tras el PR #5, `GameWorld` quedó como la interfaz de P2 y salían 7 errores de compilación en `Player`,
    `DebugPlayer` y `PlaceholderEntity`;
  - el `Player` de P2 traía su propia física y el motor lo habría movido otra vez (doble velocidad);
  - START no abría el juego porque `Game.fxml` no tenía el `Canvas` que espera `GameController`
    (`NullPointerException` en la línea 65).
- **Revisión de integración:** se comparó el código del equipo con la copia del asistente. Así se detectó que el
  sistema de vida = tiempo del equipo y el contrarreloj del asistente eran dos versiones de lo mismo, y se
  decidió no integrar el del asistente.
- **Resolución de merge:** antes de fusionar `p2` en `develop`, el asistente hizo el merge de prueba en una copia,
  encontró los conflictos que git marcaba y los que no, y dio el paso a paso con el código exacto. Comprobó con una
  simulación sin ventana que el jugador caminaba, saltaba y apuntaba dentro del motor de P1.
- **Diseño de niveles:** los 4 mapas con el formato de letras del reparto. Se revisaron con un script (que todo se
  alcance con el salto del jugador) y cargándolos en el motor del proyecto, incluido el vuelo con jetpack. Son la base
  de los `.json` actuales: al pasar a `.json` (47f1d6f) no cambió ninguna baldosa.
- **Revisión contra el enunciado:** se jugaron los 4 niveles en una simulación y se comparó el proyecto con el reparto
  y el PDF. Pendientes encontrados en `67d58d7`: las armas no se conservan entre niveles y Acesi no trae armas para
  recoger; no hay desbloqueo progresivo; el jetpack funciona en cualquier parte y el PDF dice que solo en ciertas zonas;
  desde `ea2ad5c` el mapa del nivel 2 es casi igual al del 3 (11 baldosas distintas); no hay helicópteros en el nivel 1;
  los policías y el helicóptero no tienen sprite.
- **Retirada de una funcionalidad:** el asistente buscó en el proyecto todo lo que dependía de las skins (pantalla,
  botón del menú, tono del jugador y la escena del menú) y dio el paso a paso para quitarla sin romper el resto.
  Al revisar también vio que `Skins.fxml` apuntaba a `control.SkinsController` y la clase se llama `SkinController`.
- **Documentación:**
  - el reparto del trabajo, cómo integrar las partes y los mensajes de commit sugeridos;
  - la guía del merge P2 → P1, en un documento compartido;
  - la explicación en lenguaje sencillo del rol de cada clase y de cómo funciona la `HashTable`.
  - esta sección y `AI_TRACE.md`.
### Intervenciones humanas importantes
 
- **Decisiones de diseño del equipo:**
  - el King Papa recupera **+150**, que ahora son segundos del contador porque la vida es tiempo;
  - **morir siempre reinicia el nivel**;
  - las armas se **desbloquean al recogerlas**;
  - el tiempo es parte del reto, y el calentamiento de la máquina hace que se acabe más rápido;
  - **se quita la funcionalidad de skins**: el equipo pidió el paso a paso y es quien aplica los cambios en el código.
  - el integrante de P3 pidió **ir paso a paso**, revisando cada cambio antes de aplicarlo, en vez de recibir los
    archivos completos, para no dañar el trabajo de los demás.
- **Diseño propio de un integrante:** el sistema de vida = tiempo con calor por arma y el carrito de cholado, que es
  la versión que quedó en el juego. Herramienta usada: **[COMPLETAR POR EL EQUIPO]**.
- **Reglas de código:** los `enum` en Java solo llevan constantes, sin métodos. El asistente tuvo que rehacer
  `AimDirection` y pasar su lógica a `AimVector`.
- **Arte:** el equipo dibujó los sprites del personaje (`PJ_*` y sus poses), de los jefes (`Jefe1..3`, `BolaJefe1`), de la Freidora (King Papa) y de las armas.
- **Pruebas manuales en el juego real**, que encontraron errores que el asistente no vio:
  - el nombre del nivel no se leía;
  - el jetpack no volaba con O;
  - el jetpack solo funcionaba en algunas zonas;
  - los jefes no se veían como en los sprites;
  - el nivel 2 mostraba "ACESI".
  - START no abría el juego (se encontró con una captura del error en la consola).
- **Ajustes:**
  - bajar la cadencia de disparo para que fuera más difícil;
  - cambiar el nombre del nivel 0 a "Uvavalle";
  - pedir fondos distintos por nivel para que no fuera monótono.
- **Integración:** juntar el código de los 5 integrantes en el repositorio, resolver versiones desactualizadas de
  `LevelLoader` y `EntityRegistry`, elegir qué versión del tiempo y el calor se quedaba, y hacer los commits y el push.
- **Correcciones del equipo tras la revisión del proyecto:** el `mainClass` del `pom.xml` (ahora `gui.App`, así funciona
  `mvn javafx:run`), convertir los mapas a `.json` y agregar las armas para recoger (`F` y `A`).
