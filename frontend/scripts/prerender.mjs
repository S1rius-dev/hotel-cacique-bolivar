/**
 * Prerender de las rutas públicas.
 *
 * La aplicación es una SPA: sin este paso, el HTML que recibe un rastreador
 * es un contenedor vacío y todo el metadato depende de que Google execute
 * JavaScript. Bing y la mayoría de los rastreadores de IA no lo hacen.
 *
 * Dos modos, en el mismo script:
 *   - Con Chrome: se renderiza cada ruta y se guarda el DOM resultante.
 *   - Sin Chrome: se escribe un shell con el <head> correcto por ruta.
 *
 * El segundo modo existe para que `pnpm build` nunca falle por falta de un
 * navegador, que es lo que pasaría en un entorno de CI sin Chrome instalado.
 */

import { createServer } from "node:http";
import { readFile, writeFile, mkdir, access, stat } from "node:fs/promises";
import { constants } from "node:fs";
import { join, extname, dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const DIST = join(ROOT, "dist");
const ORIGIN = "https://demo.example.com";
const SITE_NAME = "Hotel Cacique de Bolivar";

/**
 * Espejo de los metadatos de `src/features/**`. Solo se usa en el modo sin
 * navegador; si cambias un título en el React, actualiza también esta tabla.
 *
 * No se incluye ninguna tarifa aquí a propósito. Los precios llegan del
 * backend en tiempo de ejecución, así que el shell sin navegador no puede
 * conocerlos: publicarlos aquí obligaría a mantener dos precios que además
 * no coincidirían con los que ve el usuario.
 */
const ROUTES = [
  {
    path: "/",
    title: "Hotel en Amalfi, Antioquia | Hotel Cacique de Bolivar",
    description:
      "Hotel Cacique de Bolivar en Amalfi, Antioquia. 43 habitaciones con agua caliente, WiFi gratis y recepción 24 horas, a una cuadra del parque. Reserva online.",
  },
  {
    path: "/reservar",
    title: "Reservar habitación en Amalfi, Antioquia | Hotel Cacique de Bolivar",
    description:
      "Reserva tu habitación en el Hotel Cacique de Bolivar, Amalfi Antioquia. Tres tipos de habitación con las tarifas al día, agua caliente y WiFi gratis. Reserva sin intermediarios.",
  },
  {
    path: "/ayuda",
    title:
      "Preguntas frecuentes sobre tu estadía en Amalfi | Hotel Cacique de Bolivar",
    description:
      "Resuelve tus dudas sobre reservas, check-in, servicios, cómo llegar a Amalfi Antioquia y qué hacer durante tu estadía en el Hotel Cacique de Bolivar.",
  },
  {
    path: "/terminos",
    title:
      "Términos y Condiciones del hospedaje | Hotel Cacique de Bolivar",
    description:
      "Términos y condiciones de reserva y hospedaje del Hotel Cacique de Bolivar en Amalfi, Antioquia. Tarifas, cancelación, check-in y política de menores.",
  },
  {
    path: "/privacidad",
    title: "Política de Privacidad | Hotel Cacique de Bolivar",
    description:
      "Cómo Hotel Cacique de Bolivar, en Amalfi Antioquia, trata y protege sus datos personales conforme a la Ley 1581 de 2012 de Colombia.",
  },
];

const MIME = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".mjs": "text/javascript; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".svg": "image/svg+xml",
  ".webp": "image/webp",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".png": "image/png",
  ".ico": "image/x-icon",
  ".woff": "font/woff",
  ".woff2": "font/woff2",
  ".ttf": "font/ttf",
  ".webmanifest": "application/manifest+json",
  ".txt": "text/plain; charset=utf-8",
  ".xml": "application/xml; charset=utf-8",
  ".map": "application/json; charset=utf-8",
};

/**
 * `shell` es el index.html tal como lo dejó `vite build`, y se lee una sola
 * vez, antes de renderizar nada.
 *
 * Importa de verdad: la ruta `/` se prerenderiza la primera y su resultado
 * sobrescribe dist/index.html. Si el servidor volviera a leer ese archivo para
 * servir las rutas siguientes, /reservar arrancaría con el <head> de la portada
 * ya renderizado. react-helmet-async lee el head existente como línea base y
 * solo añade las etiquetas nuevas encima, así que cada página acababa
 * guardando dos <title> y dos <canonical>: el de la portada primero.
 */
async function serveDist(shell) {
  const server = createServer(async (req, res) => {
    const url = new URL(req.url ?? "/", "http://localhost");
    const pathname = decodeURIComponent(url.pathname);
    let filePath = join(DIST, pathname);

    let useShell = pathname === "/";
    if (!useShell) {
      try {
        const s = await stat(filePath);
        if (s.isDirectory()) filePath = join(filePath, "index.html");
      } catch {
        // Rewrite de SPA: rutas desconocidas y assets ausentes caen al shell.
        useShell = true;
      }
    }

    if (useShell) {
      res.writeHead(200, { "Content-Type": MIME[".html"] });
      res.end(shell);
      return;
    }

    try {
      const body = await readFile(filePath);
      res.writeHead(200, {
        "Content-Type": MIME[extname(filePath)] ?? "application/octet-stream",
      });
      res.end(body);
    } catch {
      res.writeHead(404).end("not found");
    }
  });

  await new Promise((done) => server.listen(0, "127.0.0.1", done));
  const { port } = server.address();
  return { server, base: `http://127.0.0.1:${port}` };
}

const CHROME_CANDIDATES = {
  win32: [
    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
    "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
  ],
  linux: [
    "/usr/bin/google-chrome",
    "/usr/bin/google-chrome-stable",
    "/usr/bin/chromium-browser",
    "/usr/bin/chromium",
    "/usr/bin/microsoft-edge",
    "/snap/bin/chromium",
  ],
  darwin: [
    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
    "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge",
    "/Applications/Chromium.app/Contents/MacOS/Chromium",
  ],
};

async function findChrome() {
  // Escape hatch para CI y para probar el modo degradado a mano.
  if (process.env.PRERENDER_NO_BROWSER === "1") return null;

  const fromEnv =
    process.env.PUPPETEER_EXECUTABLE_PATH ?? process.env.CHROME_PATH;
  if (fromEnv && (await exists(fromEnv, constants.X_OK))) return fromEnv;

  for (const candidate of CHROME_CANDIDATES[process.platform] ?? []) {
    if (await exists(candidate, constants.X_OK)) return candidate;
  }
  return null;
}

/**
 * `mode` distingue dos cosas que no son la misma:
 *
 *   - `F_OK` (por defecto): el archivo existe y se puede leer. Es lo que
 *     necesita el resto del script.
 *   - `X_OK`: el archivo tiene permiso de ejecución. Solo tiene sentido para un
 *     binario como Chrome.
 *
 * Pedir `X_OK` para leer `dist/index.html` rompía el build solo en Linux: `vite
 * build` lo escribe con modo 0644, sin bit de ejecución, así que `access`
 * devolvía EACCES y el script creía que `dist/` no existía. En Windows `X_OK`
 * no se aplica, y por eso el fallo solo aparecía en CI.
 */
async function exists(p, mode = constants.F_OK) {
  try {
    await access(p, mode);
    return true;
  } catch {
    return false;
  }
}

/**
 * Espera a que React haya pintado algo con contenido real.
 */
async function waitForApp(page) {
  await page.waitForFunction(
    () => {
      const root = document.getElementById("root");
      return !!root && root.innerText.trim().length > 200;
    },
    { timeout: 20_000 },
  );
}

/**
 * Los precios de habitación llegan del backend mientras corre la página. Si el
 * backend está disponible hay que esperar a que responda, o el snapshot sale
 * con las tarifas sin rellenar aunque la API sí tuviera los valores.
 *
 * Si no está disponible, la petición falla de inmediato y se agota la espera
 * sin coste real; el HTML se guarda sin precios, que es lo correcto: no
 * existen tarifas de respaldo escritas a mano.
 */
async function waitForNetwork(page) {
  try {
    await page.waitForNetworkIdle({ idleTime: 600, timeout: 5_000 });
  } catch {
    console.warn("  · sin respuesta del backend, el HTML sale sin tarifas");
  }
}

/**
 * Las secciones del sitio se revelan con `useInView`, que arranca en
 * `opacity-0 translate-y-8`. Si se captura el snapshot sin desplazar la página,
 * el texto queda en el HTML pero invisible, y ni Google ni un LLM que lo lea
 * lo cuenta como contenido.
 *
 * Se recorre varias veces porque `document.body.scrollHeight` crece según
 * cargan las imágenes: con una sola pasada, la parte inferior de la portada
 * nunca llega a entrar en el viewport y se queda sin revelar.
 */
async function settleReveals(page) {
  for (let pass = 0; pass < 6; pass++) {
    const remaining = await page.evaluate(async () => {
      const hidden = () =>
        document.querySelectorAll('[class*="opacity-0"][class*="translate-y-8"]')
          .length;

      const step = Math.max(200, Math.floor(window.innerHeight * 0.5));
      // Se recalcula en cada vuelta: las imágeneslazy alargan el documento.
      for (let y = 0; y <= document.body.scrollHeight; y += step) {
        window.scrollTo(0, y);
        await new Promise((r) => requestAnimationFrame(() => setTimeout(r, 80)));
      }
      window.scrollTo(0, document.body.scrollHeight);
      await new Promise((r) => setTimeout(r, 250));
      window.scrollTo(0, 0);
      await new Promise((r) => setTimeout(r, 350));
      return hidden();
    });

    if (remaining === 0) return;
    if (pass === 5) {
      console.warn(`  · quedan ${remaining} bloques sin revelar`);
    }
  }
  // Margen para que terminen las transiciones de opacidad.
  await new Promise((r) => setTimeout(r, 600));
}

async function renderWithBrowser(base) {
  const puppeteer = await import("puppeteer-core").then(
    (m) => m.default ?? m,
    () => null,
  );
  const executablePath = await findChrome();
  if (!puppeteer || !executablePath) return null;

  const browser = await puppeteer.launch({
    executablePath,
    headless: "shell",
    args: ["--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"],
  });

  try {
    for (const route of ROUTES) {
      const page = await browser.newPage();
      await page.setViewport({ width: 1280, height: 900 });

      // El backend no está disponible en CI; los fallos de red no deben
      // abortar el prerender.
      page.on("pageerror", () => {});
      page.on("requestfailed", () => {});

      await page.goto(`${base}${route.path}`, {
        waitUntil: "domcontentloaded",
        timeout: 30_000,
      });

      try {
        await waitForApp(page);
      } catch {
        console.warn(
          `  ! ${route.path}: la app no pintó contenido, se guarda igual`,
        );
      }

      await waitForNetwork(page);
      await settleReveals(page);

      const html = await page.evaluate(() => {
        const d = document;
        // El botón de Google Identity Services inyecta su CSS con
        // nonce="undefined" y se lleva ~40 KB por ruta. El script que lo crea
        // vuelve a insertarlo en el navegador, así que aquí sobra.
        d.querySelectorAll("[nonce]").forEach((el) => el.remove());
        return d.documentElement.outerHTML;
      });

      await writeRoute(route.path, html);
      console.log(`  ✓ ${route.path} (${html.length} bytes)`);
      await page.close();
    }
  } finally {
    await browser.close();
  }
  return true;
}

/**
 * Modo sin navegador: shell con el head correcto. Sin contenido visible, así
 * que solo sirve para que un rastreador sin JavaScript lea título, canonical y
 * Open Graph.
 *
 * Se borran las etiquetas SEO del shell antes de escribir las de la ruta, en
 * lugar de sustituirlas una a una. Es idempotente: da igual que el shell venga
 * limpio de `vite build` o ya contaminado por una ejecución anterior, porque
 * `dist/index.html` se sobrescribe al prerenderizar la portada.
 */
async function writeShells(shell) {
  for (const route of ROUTES) {
    const canonical = `${ORIGIN}${route.path === "/" ? "/" : route.path}`;
    const image = `${ORIGIN}/og-image.jpg`;
    const title = escapeAttr(route.title);
    const description = escapeAttr(route.description);

    const head = [
      `<title>${title}</title>`,
      `<meta name="description" content="${description}" />`,
      `<link rel="canonical" href="${canonical}" />`,
      `<meta name="robots" content="index, follow" />`,
      `<meta property="og:type" content="website" />`,
      `<meta property="og:site_name" content="${escapeAttr(SITE_NAME)}" />`,
      `<meta property="og:locale" content="es_CO" />`,
      `<meta property="og:title" content="${title}" />`,
      `<meta property="og:description" content="${description}" />`,
      `<meta property="og:url" content="${canonical}" />`,
      `<meta property="og:image" content="${image}" />`,
      `<meta property="og:image:width" content="1200" />`,
      `<meta property="og:image:height" content="630" />`,
      `<meta name="twitter:card" content="summary_large_image" />`,
      `<meta name="twitter:title" content="${title}" />`,
      `<meta name="twitter:description" content="${description}" />`,
      `<meta name="twitter:image" content="${image}" />`,
    ].join("\n    ");

    // Todas las sustituciones usan función: con una cadena de reemplazo, un
    // `$` seguido de dígitos del texto insertado se leería como una
    // referencia de grupo ($1, $2...) y el HTML salía corrupto. Así que
    // cualquier título o descripción puede llevar "$" sin riesgo.
    const html = stripSeoTags(shell).replace(
      "</head>",
      () => `    ${head}\n  </head>`,
    );

    await writeRoute(route.path, html);
    console.log(`  ~ ${route.path} (shell, sin navegador)`);
  }
}

function stripSeoTags(html) {
  return html
    .replace(/<title>[\s\S]*?<\/title>/gi, () => "")
    .replace(/<meta\s+name="description"[^>]*>/gi, () => "")
    .replace(/<meta\s+name="robots"[^>]*>/gi, () => "")
    .replace(/<meta\s+property="og:[^"]*"[^>]*>/gi, () => "")
    .replace(/<meta\s+name="twitter:[^"]*"[^>]*>/gi, () => "")
    .replace(/<link\s+rel="canonical"[^>]*>/gi, () => "");
}

function escapeAttr(value) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

async function writeRoute(path, html) {
  const dir = path === "/" ? DIST : join(DIST, path);
  await mkdir(dir, { recursive: true });
  await writeFile(join(dir, "index.html"), html, "utf8");
}

async function main() {
  const shellPath = join(DIST, "index.html");
  if (!(await exists(shellPath))) {
    console.error("dist/index.html no existe. Ejecuta primero `vite build`.");
    process.exit(1);
  }

  // Se lee antes de renderizar: ver la nota de serveDist().
  const shell = await readFile(shellPath, "utf8");
  const { server, base } = await serveDist(shell);
  try {
    console.log("Prerender de rutas públicas:");
    const rendered = await renderWithBrowser(base).catch((err) => {
      console.warn(`  ! No se pudo lanzar el navegador: ${err.message}`);
      return null;
    });

    if (rendered) {
      console.log("Prerender completado con navegador.");
    } else {
      console.warn(
        "  · Sin Chrome disponible: se escriben solo los metadatos por ruta.",
      );
      await writeShells(shell);
    }
  } finally {
    server.close();
  }
}

await main();
