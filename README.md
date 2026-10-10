# Papa_Bullet

Juego de acción lateral 2D inspirado en **Contra 4**, proyecto **GameHub-CIS**.
Hecho en **Java + JavaFX** por el equipo **Los 4 fantasticos + 1** para el evento **AI_GAME_JAM**.

Controlás a un científico con una máquina de papas que atraviesa cuatro niveles, enfrenta policías y
helicópteros, y derrota a un jefe al final de cada uno. Pero hay un detalle: tu vida es **tiempo**, y la
máquina se calienta cada vez que disparás.

---

## Cómo se juega

### Objetivo
Recorrer cada nivel, recoger las armas que aparecen en el camino, sobrevivir a los enemigos y vencer al jefe
final. Al completar los cuatro niveles se gana el juego. Si morís, el nivel se reinicia.

### Controles

| Tecla | Acción |
|-------|--------|
| `A` / `D` | Moverse a la izquierda / derecha |
| `W` | Apuntar hacia arriba (combinada con `A` o `D`, en diagonal) |
| `S` | Agacharse |
| `L` | Saltar |
| `K` | Disparar (sostenida, dispara al ritmo del arma) |
| `Q` | Cambiar de arma |
| `O` | Jetpack: mantenela para cargarlo (~3 s) y volar hacia la zona marcada con la mira roja |
| `Ñ` | Recargar en el carrito del King Papa |
| `Esc` | Pausa |

El personaje apunta en 8 direcciones y tiene poses propias para caminar, saltar, agacharse, apuntar y morir.

### Vida = tiempo
- La vida es un contador de **300 segundos** que baja solo, un segundo por segundo.
- Cada golpe de un enemigo te quita segundos.
- **Calentamiento:** cada disparo calienta la máquina y se enfría con el tiempo. Mientras más caliente,
  más rápido baja el contador, hasta **×4** con la máquina al rojo. La mochila del personaje se tiñe de rojo,
  titila cerca del máximo y echa vapor.
- **King Papa:** un carrito de cholado que te devuelve **+150 segundos** y enfría la máquina.

### Armas
Empezás con el Thrower Papa; las otras dos se **desbloquean al recogerlas** en el nivel.

| Arma | Qué hace | Cadencia | Calor por disparo |
|------|----------|----------|-------------------|
| **Thrower Papa** | Lanza papas bomba en parábola (arma por defecto) | 0.6 s | 0.15 |
| **French Spikes** | Una caja de papas fritas que a mitad del camino se abre en 5 espinas | 0.9 s | 0.20 |
| **Oil Slick** | Lanza aceite caliente que queda en el piso quemando a los enemigos durante 5 s | 1.4 s | 0.30 |

Todas se enfrían a razón de 0.12 por segundo.

### Enemigos
- **Policía con porra:** camina hacia vos y te golpea al tocarte.
- **Policía con pistola.**
- **Policía con escudo.**
- **Helicóptero.**
- **Jefes:** uno por nivel, con su propio tamaño, vida, patrón de disparo y una fase de enojo cuando les queda
  menos de la mitad de la vida.

### Niveles

| # | Nivel | Ambiente | Jefe |
|---|-------|----------|------|
| 0 | **UVAVALLE** | Día, montañas nevadas al fondo, ciudad en ruinas y selva. Incluye el tutorial | Robot |
| 1 | **AYTONOMA** | Atardecer en el desierto, con sol bajo y arena | El Ingeniero |
| 2 | **JABERUMBA** | Noche de fiesta, con luna, edificios con neón y concreto | El Fiestero |
| 3 | **ACESI** | Volcán: cielo rojo con brasas, árboles quemados y roca con grietas de lava | Monstruo de Piedra |

Cada nivel tiene su propio fondo con parallax y su propia paleta de terreno (`Theme`), dibujados con figuras
en código, sin imágenes de fondo.

---

## Cómo ejecutarlo

### Requisitos
- **JDK 17** o superior.
- No hace falta instalar Maven: el proyecto incluye el wrapper (`mvnw` / `mvnw.cmd`).
- JavaFX 17 se descarga solo como dependencia de Maven.

### Desde la terminal
```bash
cd Papa_Bullet_Game
./mvnw clean javafx:run          # Linux / macOS
mvnw.cmd clean javafx:run        # Windows
```

Para saltarse el menú y entrar directo al juego:
```bash
./mvnw clean javafx:run -Djavafx.args="--game"
```

### Desde un IDE (IntelliJ)
Abrí la carpeta `Papa_Bullet_Game` como proyecto Maven y ejecutá la clase **`gui.Launcher`**.
No ejecutes `gui.App` directamente: la clase `Launcher` existe para evitar el error
*"JavaFX runtime components are missing"*.

---

## Estructura del proyecto

```
Papa_Bullet/
├── README.md
├── docs/
│   ├── README_Trazabilidad.md     Trazabilidad del desarrollo
│   └── AiTrace.md                 Registro incremental del uso de IA
└── Papa_Bullet_Game/
    ├── pom.xml
    └── src/main/
        ├── java/
        │   ├── gui/               Arranque (Launcher, App) y HUD
        │   ├── control/           Controladores de las vistas: menú, juego y pausa
        │   ├── model/             Motor y lógica del juego
        │   └── structures/        Estructuras propias (HashTable)
        └── resources/assets/
            ├── levels/            nivel0.json ... nivel3.json
            ├── sprites/           Sprites del personaje, jefes y armas
            └── views/             Vistas FXML y hoja de estilos (menu.css)
```

### Paquete `model`
- **Motor:** `GameWorld`, `GameLoop`, `Collision`, `Camera`, `Entity`, `EntityRegistry`, `InputHandler`, `Config`.
- **Niveles:** `Level`, `LevelLoader`, `JsonReader`, `Background` y `Theme`.
- **Jugador:** `Player`, junto con `AimDirection` y `AimVector` para apuntar en 8 direcciones.
- **Armas:** la interfaz `Weapon` y sus implementaciones `ThrowerPapa`, `FrenchSpikes` y `OilSlick`, con sus
  proyectiles (`PapaBomba`, `FrenchFry`, `OilPuddle`) y los recogibles (`WeaponPickUp`).
- **Enemigos:** `Enemy`, `PoliceBaton`, `PolicePistol`, `PoliceShield`, `Helicopter`, `Boss` y `EnemyBullet`.
- **Otros:** `KingPapa` (el carrito), `Explosion`, `Overlay` y `Sprites` (carga y caché de imágenes).

---

## Detalles técnicos

- **Java 17 + JavaFX 17** (FXML + Canvas), construido con **Maven**.
- Ventana de **800×600**, con baldosas de 40 px (20 columnas × 15 filas visibles).
- **Niveles en JSON:** cada archivo trae el nombre y el mapa en texto, y se lee con un **lector JSON propio**
  (`JsonReader`), sin librerías externas.
- **`HashTable` propia** en `structures`, usada por `Sprites`, `EntityRegistry`, `InputHandler` y `JsonReader`.
- **Contratos entre módulos:** `Weapon`, `Entity` y `Overlay` permitieron repartir el trabajo entre 5 personas
  sin pisarse.
- **Tiempo por frame acotado:** el paso de tiempo máximo es 0.05 s, así que una pausa o un lag no causan saltos
  enormes.

---

## Desarrollo y uso de IA

El equipo documentó cómo se construyó el juego y qué papel tuvo la IA:

- [`docs/README_Trazabilidad.md`](docs/README_Trazabilidad.md): hitos, herramientas, intervenciones humanas y pendientes.
- [`docs/AiTrace.md`](docs/AiTrace.md): registro incremental de cada uso de IA (solo se agregan entradas al final).

---

## Equipo

**Los 4 fantasticos + 1** — evento **AI_GAME_JAM**, proyecto **GameHub-CIS**.

## Playthrough

https://drive.google.com/file/d/1xihT7pANEKluvspZta81B9pA6awfpQms/view?usp=drive_link

https://youtu.be/8oHrEyueY1A
