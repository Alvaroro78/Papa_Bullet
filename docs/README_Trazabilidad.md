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
 
> **Sobre el hito 12.** El asistente también generó su propia versión: reloj aparte de 4:00, calor fijo de +0.25 y
> reloj ×3 con el arma sobrecalentada. **Esa versión no quedó en el código**, porque el repositorio ya tenía la del
> equipo, que es la que se describe arriba. Está registrada en `AI_TRACE.md` como generada y no integrada.
### Herramientas de IA utilizadas

- **Claude (Anthropic)**, en sesiones de agente con acceso a una **copia** del proyecto. El modelo configurado es
  `claude-sonnet5.5`. El asistente no tenía acceso al repositorio oficial, así que no hizo commits ni push: entregó
  los archivos y el equipo los integró y los subió.

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
- **Revisión de integración:** se comparó el código del equipo con la copia del asistente. Así se detectó que el
  sistema de vida = tiempo del equipo y el contrarreloj del asistente eran dos versiones de lo mismo, y se
  decidió no integrar el del asistente.
- **Documentación:**
  - el reparto del trabajo, cómo integrar las partes y los mensajes de commit sugeridos;
  - la explicación en lenguaje sencillo del rol de cada clase y de cómo funciona la `HashTable`.
  - esta sección y `AI_TRACE.md`.
### Intervenciones humanas importantes
 
- **Decisiones de diseño del equipo:**
  - el King Papa recupera **+150**, que ahora son segundos del contador porque la vida es tiempo;
  - **morir siempre reinicia el nivel**;
  - las armas se **desbloquean al recogerlas**;
  - el tiempo es parte del reto, y el calentamiento de la máquina hace que se acabe más rápido.
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
- **Ajustes:**
  - bajar la cadencia de disparo para que fuera más difícil;
  - cambiar el nombre del nivel 0 a "Uvavalle";
  - pedir fondos distintos por nivel para que no fuera monótono.
- **Integración:** juntar el código de los 5 integrantes en el repositorio, resolver versiones desactualizadas de
  `LevelLoader` y `EntityRegistry`, elegir qué versión del tiempo y el calor se quedaba, y hacer los commits y el push.


### Limitaciones y pendientes finales

- **Jefes:** quedó abierto por qué no se veían como en los sprites; se esperaba una captura para diagnosticarlo.
- **Equilibrio:** falta ajustar la vida de los jefes, el daño de los enemigos y el calentamiento después de jugar.
- **No hecho:** el juego no tiene sonido, y la pantalla de victoria es solo un texto.
