package model;

import javafx.scene.paint.Color;

/**
 * Paleta de un nivel: el cielo, las tres capas del fondo y el terreno. Background y Level dibujan
 * siempre las mismas figuras, solo cambian los colores y el tipo de capa cercana.
 * El nivel i usa ALL[i] (lo asigna LevelLoader.loadIndex).
 */
public record Theme(
        Color[] sky, Color stars,                 // stars = null: sin estrellas
        Color orb, double orbY, double orbR,      // sol o luna (orb = null: no hay)
        Color farLit, Color farShade, Color peakLit, Color peakShade,   // montañas y su punta (nieve, lava...)
        Color wall, Color wallDark, Color window, double litWindows,    // edificios; litWindows = parte encendida
        int near, Color nearDark, Color nearLit,  // near: 0 selva, 1 desierto, 2 ciudad de noche, 3 tierra quemada
        Color[] rock, Color stone, Color stoneLit, Color edge,         // baldosas '#': de claro a hondo
        Color top, Color topLit, Color topDark) { // la capa de encima (pasto, arena, neón, ceniza)

    public static final int JUNGLE = 0, DESERT = 1, NIGHT_CITY = 2, BURNT = 3;

    private static Color c(String hex) { return Color.web(hex); }

    private static Color[] cs(String... hex) {
        Color[] out = new Color[hex.length];
        for (int i = 0; i < hex.length; i++) out[i] = c(hex[i]);
        return out;
    }

    public static final Theme[] ALL = {
            // 0 UNIVALLE: día, Farallones nevados, Cali en ruinas, selva
            new Theme(cs("#0b1640", "#13235a", "#1b3374", "#24478f", "#2f5ea9", "#3e77c2", "#5291d6", "#6daae3", "#8cc2ec"),
                    c("#dfe9ff"), null, 0, 0,
                    c("#7f9fc6"), c("#557aa6"), c("#e6eef8"), c("#b9c9dd"),
                    c("#2c3e63"), c("#223152"), c("#d9b25a"), 0.07,
                    JUNGLE, c("#1d5b33"), c("#2b7a41"),
                    cs("#8a5a2c", "#7a4e26", "#6a4320"), c("#5c3a1b"), c("#a8723c"), c("#3e2510"),
                    c("#3c9c32"), c("#7bd052"), c("#2a7524")),

            // 1 AYTONOMA: atardecer en el desierto, sol bajo, cactus, arena con pasto seco
            new Theme(cs("#2a1b4a", "#4a2560", "#7a2f68", "#b0406a", "#d9586a", "#ef7a5e", "#f79d5c", "#fbbf6a", "#fdd88a"),
                    null, c("#ffe08a"), 175, 55,
                    c("#b0607a"), c("#7e3f62"), c("#f2b08a"), c("#c98070"),
                    c("#5a2a4a"), c("#43203a"), c("#ffcf6a"), 0.10,
                    DESERT, c("#9a6a2c"), c("#c4924a"),
                    cs("#d9a55a", "#c48f48", "#a8763a"), c("#8a5e2c"), c("#f0c47a"), c("#6a4320"),
                    c("#c9b040"), c("#efe07a"), c("#9a8428")),

            // 2 JABERUMBA: noche de fiesta, luna, edificios con neón, concreto con borde rosado
            new Theme(cs("#05040f", "#0a0820", "#110c30", "#1a1040", "#24144e", "#2e185c", "#3a1d68", "#472272", "#55287c"),
                    c("#ffffff"), c("#f4f0d8"), 90, 30,
                    c("#2b2552"), c("#1d1940"), c("#4a4280"), c("#3a3468"),
                    c("#181530"), c("#0f0c22"), c("#ff4fd8"), 0.25,
                    NIGHT_CITY, c("#120f26"), c("#ffe680"),
                    cs("#5a5a6a", "#4a4a5a", "#3a3a48"), c("#2e2e3a"), c("#8a8aa0"), c("#22222c"),
                    c("#2a2a36"), c("#ff4fd8"), c("#a03090")),

            // 3 ACESI: volcán, cielo rojo con brasas, árboles quemados, roca negra con grietas de lava
            new Theme(cs("#1a0505", "#2a0808", "#3d0c0a", "#55120c", "#701a0e", "#8c2410", "#a83412", "#c44a16", "#d9661c"),
                    c("#ffb040"), null, 0, 0,
                    c("#3a2420"), c("#241614"), c("#ff7a1a"), c("#d94a10"),
                    c("#2a1a1a"), c("#1a0f0f"), c("#ff6a1a"), 0.12,
                    BURNT, c("#1a1010"), c("#5a3e36"),
                    cs("#3a3436", "#2e292b", "#242022"), c("#ff6a1a"), c("#ffd04a"), c("#141012"),
                    c("#4a4244"), c("#7a7072"), c("#2e2a2b")),
    };
}