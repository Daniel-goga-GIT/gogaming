// Comprueba las traducciones:
//  1. es.json y en.json tienen exactamente las mismas claves.
//  2. Toda clave literal usada en el código ('X.Y' | translate, translate.instant('X.Y'), titulo: 'X.Y'...) existe.
// Uso: npm run i18n:check  (sale con código 1 si hay problemas)
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join } from 'node:path';

const raiz = new URL('..', import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, '$1');
const i18n = join(raiz, 'src/assets/i18n');

const aplanar = (obj, prefijo = '') =>
  Object.entries(obj).flatMap(([k, v]) => (typeof v === 'object' && v !== null ? aplanar(v, `${prefijo}${k}.`) : [`${prefijo}${k}`]));

const claves = Object.fromEntries(['es', 'en'].map(l => [l, new Set(aplanar(JSON.parse(readFileSync(join(i18n, `${l}.json`), 'utf8'))))]));
const problemas = [];

for (const [a, b] of [['es', 'en'], ['en', 'es']]) {
  for (const k of claves[a]) if (!claves[b].has(k)) problemas.push(`Falta en ${b}.json: ${k}`);
}

const archivos = dir => readdirSync(dir).flatMap(f => {
  const ruta = join(dir, f);
  return statSync(ruta).isDirectory() ? archivos(ruta) : /\.(ts|html)$/.test(f) && !f.endsWith('.spec.ts') ? [ruta] : [];
});

// Literales con forma de clave i18n: SECCION.CLAVE en mayúsculas (CATEGORY.Raton se genera dinámicamente y no se busca).
const patron = /['"]([A-Z][A-Z0-9_]*\.[A-Z][A-Z0-9_]*(?:\.[A-Z0-9_]+)*)['"]/g;
for (const archivo of archivos(join(raiz, 'src/app'))) {
  const texto = readFileSync(archivo, 'utf8');
  for (const [, clave] of texto.matchAll(patron)) {
    if (!claves.es.has(clave)) problemas.push(`${archivo.slice(raiz.length)}: no existe la clave ${clave}`);
  }
}

if (problemas.length) {
  console.error([...new Set(problemas)].join('\n'));
  console.error(`\n${problemas.length} problema(s) de traducción.`);
  process.exit(1);
}
console.log(`Traducciones correctas: ${claves.es.size} claves en es.json y en.json.`);
