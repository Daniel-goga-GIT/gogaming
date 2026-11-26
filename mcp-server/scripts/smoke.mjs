// Prueba de humo: arranca el servidor por stdio como lo haría Claude, lista las herramientas
// y llama a cada una contra el backend (que debe estar en marcha). Uso: npm run smoke
import { Client } from '@modelcontextprotocol/sdk/client/index.js';
import { StdioClientTransport } from '@modelcontextprotocol/sdk/client/stdio.js';

const transport = new StdioClientTransport({ command: process.execPath, args: ['dist/index.js'], env: { ...process.env } });
const client = new Client({ name: 'smoke-test', version: '1.0.0' });
await client.connect(transport);

const { tools } = await client.listTools();
console.log('Herramientas:', tools.map(t => t.name).join(', '));

const llamadas = [
  ['buscar_productos', { texto: 'pulsar', max: 3 }],
  ['ver_producto', { id: 39 }],
  ['consultar_stock', { umbral: 5 }],
  ['resumen_pedidos', { dias: 30 }],
  ['buscar_productos', { texto: '' }], // entrada no válida: la rechaza el esquema
];
let fallos = 0;
for (const [name, args] of llamadas) {
  try {
    const r = await client.callTool({ name, arguments: args });
    const texto = r.content.map(c => c.text).join('\n');
    console.log(`\n── ${name} ${JSON.stringify(args)}${r.isError ? ' [isError]' : ''}\n${texto}`);
  } catch (e) {
    console.log(`\n── ${name} ${JSON.stringify(args)} → rechazado: ${e.message.split('\n')[0]}`);
    if (name !== 'buscar_productos' || args.texto !== '') fallos++;
  }
}
await client.close();
process.exit(fallos ? 1 : 0);
