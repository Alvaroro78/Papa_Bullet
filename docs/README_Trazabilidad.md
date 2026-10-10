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
- Estado del repositorio al empezar: **[COMPLETAR DESDE GIT]** (primer commit, hash y fecha).

### Hitos principales, en orden

| # | Hito | Commit / fecha |
|---|------|----------------|
| 1 | Vistas: menú principal con tablas de madera y paisaje, vista de juego con un Canvas donde viven los niveles y menú de pausa con el juego visible detrás | [COMPLETAR DESDE GIT] |
| 2 | Reparto del trabajo en 5 módulos (P1 a P5) con contratos entre ellos (`Weapon`, `Entity`, `Overlay`) | [COMPLETAR DESDE GIT] |
| 3 | Jugador (P2): apuntar en 8 direcciones, agacharse, saltar y bajar de plataformas, cambiar de arma, recargar vida con el King Papa (+150), jetpack y poses del sprite | [COMPLETAR DESDE GIT] |
| 4 | Pasada de afinado: se corrigió que el King Papa nunca recargaba y se listó lo que faltaba | [COMPLETAR DESDE GIT] |
| 5 | `HashTable` propia en `structures`, niveles en `.json` con lector JSON propio, y menús con el mismo estilo y colores de buen contraste | [COMPLETAR DESDE GIT] |
| 6 | Armas (Thrower Papa, French Spikes, Oil Slick) que se desbloquean al recogerlas, enemigos (3 policías y helicóptero), 4 jefes, HUD, tutorial en el nivel 0 y pantalla de Skins | [COMPLETAR DESDE GIT] |
| 7 | Correcciones: nombre del nivel legible en el HUD (CSS) y constructor de `Level` que no coincidía con un `LevelLoader` desactualizado | [COMPLETAR DESDE GIT] |
| 8 | Jetpack sin zonas: funciona en cualquier parte del nivel | [COMPLETAR DESDE GIT] |
| 9 | Cadencia de disparo por arma: Thrower 0.6 s, Spikes 0.9 s y Oil 1.4 s. Con K sostenida dispara a ese ritmo | [COMPLETAR DESDE GIT] |
| 10 | Fondo y terreno distintos por nivel (`Theme`): selva, desierto, ciudad de noche y volcán | [COMPLETAR DESDE GIT] |
| 11 | Nombres corregidos: el nivel 0 pasa a **UVAVALLE** y el nivel 2 vuelve a **JABERUMBA**. **Checkpoint validado:** "Fondos personalizados x nivel y corrección de nombres" | [COMPLETAR DESDE GIT] |
| 12 | Contrarreloj de 4:00 por nivel y calentamiento del arma: con el arma sobrecalentada el reloj corre 3 veces más rápido | [COMPLETAR DESDE GIT] |

### Herramientas de IA utilizadas

- **Claude (Anthropic)**, en sesiones de agente con acceso a una **copia** del proyecto. El modelo configurado es
  `claude-opus-5-5`. El asistente no tenía acceso al repositorio oficial, así que no hizo commits ni push: entregó
  los archivos y el equipo los integró y los subió.
- **"Ponytail"**: un modo de trabajo del asistente que exige la solución más simple que funcione. El equipo lo pidió
  para mantener el mismo nivel de complejidad en todo el código.
- Otras herramientas de IA usadas por los demás integrantes: **[COMPLETAR POR EL EQUIPO]**.

### Ejemplos de tareas donde la IA fue relevante

- **Diseño de vistas:** el FXML y el CSS de los menús de inicio, pausa y Skins, y el fondo con parallax dibujado con figuras, sin imágenes.
- **Implementación:** el jugador completo (P2), las armas y sus proyectiles, los enemigos y jefes, la `HashTable`, el lector JSON, el HUD, el `Theme` por nivel, el contrarreloj y el calentamiento.
- **Depuración:**
  - el King Papa no recargaba porque la condición estaba invertida y nunca se marcaba como usado;
  - el jetpack no volaba porque dependía de zonas invisibles;
  - el texto del nivel no se leía porque el contorno se dibujaba encima del relleno;
  - el aceite no le pegaba a un policía que caminaba;
  - un error de compilación por un `LevelLoader` desactualizado.
- **Pruebas:** tests de JUnit para el apuntado, el King Papa, las poses, el jetpack, la cadencia, las armas, los enemigos, los niveles JSON, la `HashTable`, el tema por nivel, el calentamiento y el contrarreloj.
- **Documentación:** el reparto del trabajo, cómo integrar las partes, los mensajes de commit sugeridos y esta sección.

### Intervenciones humanas importantes

- **Decisiones de diseño del equipo:**
  - el King Papa da +150 de **vida**;
  - **morir siempre reinicia el nivel**;
  - las armas se **desbloquean al recogerlas**;
  - 4 minutos por nivel, y el sobrecalentamiento acelera el reloj.
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
  `LevelLoader` y `EntityRegistry`, y hacer los commits y el push.

### Limitaciones y pendientes finales

- **Pruebas:** el asistente corrió los tests contra una imitación de JavaFX y JUnit, porque desde su entorno no
  pudo descargar las librerías reales. Los resultados en JavaFX real (`mvn test` y `mvn javafx:run`) los debe
  confirmar el equipo: **[COMPLETAR CON RESULTADO]**.
- **Sprites:** faltaba copiar al repositorio las poses del personaje (`PJ_agachado`, `PJ_arriba`, `PJ_diagonal`,
  `PJ_salto`, `PJ_muerto`) y los íconos de las armas (`PapaBomba`, `FrenchSpikes`, `CharcoAceite`).
- **Jefes:** quedó abierto por qué no se veían como en los sprites; se esperaba una captura para diagnosticarlo.
- **Equilibrio:** falta ajustar la vida de los jefes, el daño de los enemigos y el calentamiento después de jugar.
- **No hecho:** el juego no tiene sonido, y la pantalla de victoria es solo un texto.
