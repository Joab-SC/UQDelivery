// Genera los diagramas entidad-relación de UQDelivery desde una sola definición del modelo:
//   - diagrama-er.png y diagrama-er-sprint-1.png (notación pata de gallo, líneas de celda a celda)
//   - el código Mermaid de cada uno, que se copia en diagrama-er.md
//
// Uso (desde la raíz del repo; necesita Node y Google Chrome o Microsoft Edge, sin instalar nada más):
//   node docs/modelo-datos/generar-diagramas.js            → actualiza los dos PNG
//   node docs/modelo-datos/generar-diagramas.js --mermaid  → imprime el Mermaid para pegarlo en diagrama-er.md
//
// Si cambia el modelo: se edita `tablas`, `enums` y las relaciones de abajo, se generan los PNG y se
// actualiza el Mermaid de diagrama-er.md.
const fs = require('fs');
const os = require('os');
const path = require('path');
const { execFileSync } = require('child_process');
const OUT = __dirname;

// ---------- Modelo ----------
const tablas = {
  usuarios: [
    ['PK', 'id', 'BIGINT'], ['', 'nombre', 'VARCHAR(100)'], ['', 'correo', 'VARCHAR(150)'],
    ['', 'contrasena_hash', 'VARCHAR(100)'], ['', 'telefono', 'VARCHAR(20)'], ['', 'rol', 'Rol'],
    ['', 'activo', 'BOOLEAN'], ['', 'notificaciones_activas', 'BOOLEAN'], ['', 'fecha_registro', 'TIMESTAMP'],
  ],
  tokens_recuperacion: [
    ['PK', 'id', 'BIGINT'], ['FK', 'usuario_id', 'BIGINT'], ['', 'token', 'VARCHAR(36)'],
    ['', 'fecha_expiracion', 'TIMESTAMP'], ['', 'usado', 'BOOLEAN'], ['', 'fecha_creacion', 'TIMESTAMP'],
  ],
  configuracion_tarifa: [
    ['PK', 'id', 'BIGINT'], ['', 'tarifa_base', 'NUMERIC'], ['', 'precio_por_km', 'NUMERIC'],
    ['', 'precio_por_kg', 'NUMERIC'], ['', 'factor_ruta', 'NUMERIC'], ['', 'distancia_minima_km', 'NUMERIC'],
    ['', 'peso_maximo_kg', 'NUMERIC'], ['', 'medida_maxima_cm', 'NUMERIC'], ['FK', 'actualizado_por_id', 'BIGINT'],
    ['', 'fecha_actualizacion', 'TIMESTAMP'],
  ],
  envios: (completo) => [
    ['PK', 'id', 'BIGINT'], ['', 'codigo_seguimiento', 'VARCHAR(12)'], ['FK', 'emisor_id', 'BIGINT'],
    ['FK', 'repartidor_id', 'BIGINT'], ['', 'direccion_origen', 'VARCHAR(200)'], ['', 'ciudad_origen', 'Ciudad'],
    ['', 'direccion_destino', 'VARCHAR(200)'], ['', 'ciudad_destino', 'Ciudad'],
    ['', 'nombre_destinatario', 'VARCHAR(100)'], ['', 'telefono_destinatario', 'VARCHAR(20)'],
    ['', 'correo_destinatario', 'VARCHAR(150)'], ['', 'descripcion', 'VARCHAR(500)'], ['', 'peso_kg', 'NUMERIC'],
    ['', 'largo_cm', 'NUMERIC'], ['', 'ancho_cm', 'NUMERIC'], ['', 'alto_cm', 'NUMERIC'],
    ['', 'distancia_km', 'NUMERIC'], ['', 'costo', 'NUMERIC'], ['', 'estado', 'EstadoEnvio'],
    ...(completo ? [['', 'latitud_destino', 'NUMERIC'], ['', 'longitud_destino', 'NUMERIC']] : []),
    ['', 'fecha_creacion', 'TIMESTAMP'], ['', 'fecha_entrega', 'TIMESTAMP'], ['', 'fecha_actualizacion', 'TIMESTAMP'],
  ],
  perfil_repartidor: [
    ['PK, FK', 'usuario_id', 'BIGINT'], ['', 'tipo_vehiculo', 'TipoVehiculo'], ['', 'placa', 'VARCHAR(10)'],
    ['', 'disponible', 'BOOLEAN'], ['', 'en_turno', 'BOOLEAN'], ['', 'fecha_actualizacion', 'TIMESTAMP'],
  ],
  historial_estados: [
    ['PK', 'id', 'BIGINT'], ['FK', 'envio_id', 'BIGINT'], ['', 'estado_anterior', 'EstadoEnvio'],
    ['', 'estado_nuevo', 'EstadoEnvio'], ['FK', 'usuario_id', 'BIGINT'], ['', 'observacion', 'VARCHAR(300)'],
    ['', 'fecha', 'TIMESTAMP'],
  ],
  asignaciones: [
    ['PK', 'id', 'BIGINT'], ['FK', 'envio_id', 'BIGINT'], ['FK', 'repartidor_id', 'BIGINT'],
    ['FK', 'asignado_por_id', 'BIGINT'], ['', 'estado', 'EstadoAsignacion'], ['', 'motivo', 'VARCHAR(300)'],
    ['', 'orden_ruta', 'INTEGER'], ['', 'fecha_asignacion', 'TIMESTAMP'], ['', 'fecha_fin', 'TIMESTAMP'],
  ],
  novedades: [
    ['PK', 'id', 'BIGINT'], ['FK', 'envio_id', 'BIGINT'], ['FK', 'repartidor_id', 'BIGINT'],
    ['', 'tipo', 'TipoNovedad'], ['', 'descripcion', 'VARCHAR(500)'], ['', 'latitud', 'NUMERIC'],
    ['', 'longitud', 'NUMERIC'], ['', 'resuelta', 'BOOLEAN'], ['', 'fecha_reporte', 'TIMESTAMP'],
  ],
  evidencias_entrega: [
    ['PK', 'id', 'BIGINT'], ['FK', 'envio_id', 'BIGINT'], ['FK', 'repartidor_id', 'BIGINT'],
    ['', 'tipo', 'TipoEvidencia'], ['', 'archivo_url', 'VARCHAR(500)'], ['', 'codigo_confirmacion', 'VARCHAR(10)'],
    ['', 'nombre_receptor', 'VARCHAR(100)'], ['', 'fecha_captura', 'TIMESTAMP'], ['', 'fecha_sincronizacion', 'TIMESTAMP'],
  ],
  notificaciones: [
    ['PK', 'id', 'BIGINT'], ['FK', 'envio_id', 'BIGINT'], ['FK', 'usuario_id', 'BIGINT'],
    ['', 'correo_destino', 'VARCHAR(150)'], ['', 'tipo', 'TipoNotificacion'], ['', 'canal', 'CanalNotificacion'],
    ['', 'asunto', 'VARCHAR(200)'], ['', 'mensaje', 'VARCHAR(1000)'], ['', 'estado', 'EstadoNotificacion'],
    ['', 'leida', 'BOOLEAN'], ['', 'fecha', 'TIMESTAMP'],
  ],
  ubicaciones_repartidor: [
    ['PK', 'id', 'BIGINT'], ['FK', 'repartidor_id', 'BIGINT'], ['FK', 'envio_id', 'BIGINT'],
    ['', 'latitud', 'NUMERIC'], ['', 'longitud', 'NUMERIC'], ['', 'fecha', 'TIMESTAMP'],
  ],
};

const enums = {
  Rol: ['ADMIN', 'EMISOR', 'REPARTIDOR'],
  Ciudad: ['ARMENIA', 'PEREIRA', 'MANIZALES', 'CALARCA', 'MONTENEGRO'],
  EstadoEnvio: ['PENDIENTE', 'ASIGNADO', 'EN_CAMINO', 'ENTREGADO', 'CANCELADO'],
  TipoVehiculo: ['MOTO', 'BICICLETA', 'CARRO', 'CAMIONETA'],
  EstadoAsignacion: ['ACTIVA', 'RECHAZADA', 'REASIGNADA', 'FINALIZADA'],
  TipoNovedad: ['DIRECCION_ERRONEA', 'DESTINATARIO_AUSENTE', 'PAQUETE_DANADO', 'ACCESO_RESTRINGIDO', 'OTRO'],
  TipoEvidencia: ['FIRMA', 'FOTO', 'CODIGO'],
  TipoNotificacion: ['CAMBIO_ESTADO', 'CANCELACION', 'ASIGNACION', 'NOVEDAD'],
  CanalNotificacion: ['CORREO', 'APLICACION'],
  EstadoNotificacion: ['PENDIENTE', 'ENVIADA', 'FALLIDA', 'OMITIDA'],
};

// Relación: [tabla con la FK o columna, columna, destino, columna destino (null = enum), cardinalidad origen, destino]
// Cardinalidades: 'n0' = cero o muchos, 'u0' = cero o uno, 'u1' = exactamente uno.
const R = (de, col, a, colA, cDe, cA, ruta) => ({ de, col, a, colA, cDe, cA, ruta });

// ---------- Medidas ----------
const HEAD = 38, ROW = 30, KEYW = 62, CH = 7.9, FONT = 'Segoe UI, Arial, sans-serif';
const COLORS = {
  head: '#1565D8', headEnum: '#5B6675', borde: '#2B3038', alt: '#F1F3F6', linea: '#3A4049',
  texto: '#1E2228', tipo: '#556070',
};

function medirTabla(nombre, completo) {
  const esEnum = !!enums[nombre];
  if (esEnum) {
    const filas = enums[nombre];
    const w = Math.max(180, Math.ceil(Math.max(...filas.map((f) => f.length), nombre.length + 8) * CH) + 40);
    return { nombre, esEnum, filas, w, h: HEAD + filas.length * ROW };
  }
  const def = typeof tablas[nombre] === 'function' ? tablas[nombre](completo) : tablas[nombre];
  const nameW = Math.ceil(Math.max(...def.map((f) => f[1].length)) * CH) + 26;
  const typeW = Math.ceil(Math.max(...def.map((f) => f[2].length)) * CH) + 26;
  const w = Math.max(KEYW + nameW + typeW, Math.ceil(nombre.length * 8.6) + 40);
  return { nombre, esEnum, filas: def, nameW: w - KEYW - typeW, typeW, w, h: HEAD + def.length * ROW };
}

// ---------- Layout y dibujo ----------
function construir(cfg) {
  const t = {};
  let x = cfg.margenIzq;
  cfg.columnas.forEach((col, ci) => {
    const medidas = col.tablas.map((n) => medirTabla(n, cfg.completo));
    const ancho = Math.max(...medidas.map((m) => m.w));
    let y = cfg.margenSup;
    medidas.forEach((m, i) => {
      if (col.y && col.y[i] != null) y = col.y[i];
      t[m.nombre] = { ...m, x, y, col: ci };
      y += m.h + 44;
    });
    col.x = x;
    col.ancho = ancho;
    x += ancho + (cfg.gaps[ci] ?? 120);
  });

  const filaY = (tabla, col) => {
    const tb = t[tabla];
    if (col == null) return tb.y + HEAD / 2;
    const i = tb.filas.findIndex((f) => f[1] === col);
    if (i < 0) throw new Error(`No existe ${tabla}.${col}`);
    return tb.y + HEAD + i * ROW + ROW / 2;
  };
  const lado = (tabla, s) => (s === 'L' ? t[tabla].x : t[tabla].x + t[tabla].w);
  const laneX = (gap, i) => {
    // gap -1 = afuera a la izquierda; gap k = entre la columna k y la k+1
    if (gap < 0) return cfg.columnas[0].x - 26 - i * 20;
    const c = cfg.columnas[gap];
    return c.x + c.ancho + 26 + i * 20;
  };

  const conectores = cfg.relaciones.map((r) => {
    const [sDe, sA, gap, i] = r.ruta;
    const p1 = { x: lado(r.de, sDe), y: filaY(r.de, r.col) };
    const p2 = { x: lado(r.a, sA), y: filaY(r.a, r.colA) };
    const lx = laneX(gap, i);
    const pts = p1.y === p2.y && gap === 'directo' ? [p1, p2] : [p1, { x: lx, y: p1.y }, { x: lx, y: p2.y }, p2];
    return { ...r, pts };
  });

  // Segmentos verticales para detectar cruces
  const verticales = [];
  conectores.forEach((c, ci) => {
    for (let k = 0; k < c.pts.length - 1; k++) {
      const a = c.pts[k], b = c.pts[k + 1];
      if (a.x === b.x && a.y !== b.y) verticales.push({ ci, x: a.x, y1: Math.min(a.y, b.y), y2: Math.max(a.y, b.y) });
    }
  });

  let svg = '';
  const W = Math.max(...Object.values(t).map((m) => m.x + m.w)) + cfg.margenDer;
  const H = Math.max(...Object.values(t).map((m) => m.y + m.h)) + 50;

  // Líneas
  conectores.forEach((c, ci) => {
    let d = `M ${c.pts[0].x} ${c.pts[0].y}`;
    for (let k = 1; k < c.pts.length; k++) {
      const a = c.pts[k - 1], b = c.pts[k];
      if (a.y === b.y && a.x !== b.x) {
        const dir = Math.sign(b.x - a.x);
        const cruces = verticales
          .filter((v) => v.ci !== ci && v.x > Math.min(a.x, b.x) + 8 && v.x < Math.max(a.x, b.x) - 8
            && a.y > v.y1 + 1 && a.y < v.y2 - 1)
          .map((v) => v.x)
          .sort((p, q) => (p - q) * dir);
        cruces.forEach((cx) => {
          d += ` L ${cx - 6 * dir} ${a.y} A 6 6 0 0 ${dir > 0 ? 1 : 0} ${cx + 6 * dir} ${a.y}`;
        });
      }
      d += ` L ${b.x} ${b.y}`;
    }
    svg += `<path d="${d}" fill="none" stroke="${COLORS.linea}" stroke-width="1.3"/>`;
  });

  // Marcas de pata de gallo
  const marca = (p, dx, tipo) => {
    let s = '';
    const bar = (o) => `<line x1="${p.x + o * dx}" y1="${p.y - 8}" x2="${p.x + o * dx}" y2="${p.y + 8}" stroke="${COLORS.linea}" stroke-width="1.3"/>`;
    const circ = (o) => `<circle cx="${p.x + o * dx}" cy="${p.y}" r="5" fill="#fff" stroke="${COLORS.linea}" stroke-width="1.3"/>`;
    if (tipo === 'n0') {
      const ax = p.x + 18 * dx;
      s += `<path d="M ${ax} ${p.y} L ${p.x} ${p.y - 9} M ${ax} ${p.y} L ${p.x} ${p.y + 9}" stroke="${COLORS.linea}" stroke-width="1.3" fill="none"/>`;
      s += circ(26);
    } else if (tipo === 'u1') {
      s += bar(9) + bar(15);
    } else if (tipo === 'u0') {
      s += bar(9) + circ(21);
    }
    return s;
  };
  // Varias líneas pueden llegar a la misma celda (p. ej. usuarios.id): ahí se dibuja una sola marca.
  // Si mezclan "uno" y "cero o uno", se dibuja "uno" (la opcionalidad se ve en el lado de la FK y en el Mermaid).
  const finales = new Map();
  conectores.forEach((c) => {
    const n = c.pts.length;
    svg += marca(c.pts[0], Math.sign(c.pts[1].x - c.pts[0].x), c.cDe);
    const p = c.pts[n - 1], dx = Math.sign(c.pts[n - 2].x - p.x);
    const k = `${p.x},${p.y},${dx}`;
    const prev = finales.get(k);
    finales.set(k, { p, dx, tipo: prev && prev.tipo !== c.cA ? 'u1' : c.cA });
  });
  finales.forEach(({ p, dx, tipo }) => { svg += marca(p, dx, tipo); });

  // Tablas
  Object.values(t).forEach((m) => {
    const id = `clip-${m.nombre}`;
    svg += `<clipPath id="${id}"><rect x="${m.x}" y="${m.y}" width="${m.w}" height="${m.h}" rx="9"/></clipPath>`;
    svg += `<g clip-path="url(#${id})">`;
    svg += `<rect x="${m.x}" y="${m.y}" width="${m.w}" height="${m.h}" fill="#fff"/>`;
    svg += `<rect x="${m.x}" y="${m.y}" width="${m.w}" height="${HEAD}" fill="${m.esEnum ? COLORS.headEnum : COLORS.head}"/>`;
    m.filas.forEach((f, i) => {
      if (i % 2 === 1) svg += `<rect x="${m.x}" y="${m.y + HEAD + i * ROW}" width="${m.w}" height="${ROW}" fill="${COLORS.alt}"/>`;
    });
    svg += `</g>`;
    const titulo = m.esEnum
      ? `<tspan font-weight="400" font-size="13">«enum» </tspan>${m.nombre}`
      : m.nombre;
    svg += `<text x="${m.x + m.w / 2}" y="${m.y + HEAD / 2 + 5}" text-anchor="middle" font-size="15" font-weight="600" fill="#fff">${titulo}</text>`;
    m.filas.forEach((f, i) => {
      const cy = m.y + HEAD + i * ROW + ROW / 2 + 5;
      if (m.esEnum) {
        svg += `<text x="${m.x + 16}" y="${cy}" font-size="14" fill="${COLORS.texto}">${f}</text>`;
      } else {
        svg += `<text x="${m.x + KEYW / 2}" y="${cy}" text-anchor="middle" font-size="13" font-weight="600" fill="${COLORS.texto}">${f[0]}</text>`;
        svg += `<text x="${m.x + KEYW + 10}" y="${cy}" font-size="14" fill="${COLORS.texto}">${f[1]}</text>`;
        svg += `<text x="${m.x + KEYW + m.nameW + 10}" y="${cy}" font-size="13.5" fill="${COLORS.tipo}">${f[2]}</text>`;
      }
    });
    if (!m.esEnum) {
      svg += `<line x1="${m.x + KEYW}" y1="${m.y + HEAD}" x2="${m.x + KEYW}" y2="${m.y + m.h}" stroke="#C9CED6"/>`;
      svg += `<line x1="${m.x + KEYW + m.nameW}" y1="${m.y + HEAD}" x2="${m.x + KEYW + m.nameW}" y2="${m.y + m.h}" stroke="#C9CED6"/>`;
    }
    svg += `<rect x="${m.x}" y="${m.y}" width="${m.w}" height="${m.h}" rx="9" fill="none" stroke="${COLORS.borde}" stroke-width="1.6"/>`;
  });

  const doc = `<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}" viewBox="0 0 ${W} ${H}" font-family="${FONT}">`
    + `<rect width="${W}" height="${H}" fill="#fff"/>${svg}</svg>`;
  return { doc, W, H, t };
}

// ---------- Mermaid ----------
const SIM_A = { u1: '||', u0: '|o' };
const SIM_DE = { n0: 'o{', u0: 'o|' };
function mermaid(cfg) {
  const nombres = cfg.columnas.flatMap((c) => c.tablas);
  let s = 'erDiagram\n';
  nombres.filter((n) => !enums[n]).forEach((n) => {
    const def = typeof tablas[n] === 'function' ? tablas[n](cfg.completo) : tablas[n];
    s += `    ${n} {\n`;
    def.forEach(([k, c, ti]) => { s += `        ${ti} ${c}${k ? ' ' + k.replace(', ', ', ') : ''}\n`; });
    s += '    }\n';
  });
  nombres.filter((n) => enums[n]).forEach((n) => {
    s += `    ${n} {\n`;
    enums[n].forEach((v) => { s += `        enum ${v}\n`; });
    s += '    }\n';
  });
  s += '\n';
  cfg.relaciones.forEach((r) => { s += `    ${r.a} ${SIM_A[r.cA]}--${SIM_DE[r.cDe]} ${r.de} : ""\n`; });
  return s;
}

// ---------- Diagramas ----------
const sprint1 = {
  completo: false, margenIzq: 60, margenSup: 40, margenDer: 50,
  columnas: [
    { tablas: ['configuracion_tarifa', 'tokens_recuperacion', 'Rol', 'Ciudad'], y: [40, null, null, null] },
    { tablas: ['usuarios', 'envios'], y: [40, 410] },
    { tablas: ['EstadoEnvio'], y: [1000] },
  ],
  gaps: [130, 110],
  relaciones: [
    R('configuracion_tarifa', 'actualizado_por_id', 'usuarios', 'id', 'n0', 'u0', ['R', 'L', 0, 0]),
    R('tokens_recuperacion', 'usuario_id', 'usuarios', 'id', 'n0', 'u1', ['R', 'L', 0, 0]),
    R('envios', 'emisor_id', 'usuarios', 'id', 'n0', 'u1', ['L', 'L', 0, 0]),
    R('envios', 'repartidor_id', 'usuarios', 'id', 'n0', 'u0', ['L', 'L', 0, 0]),
    R('usuarios', 'rol', 'Rol', null, 'n0', 'u1', ['L', 'R', 0, 2]),
    R('envios', 'ciudad_origen', 'Ciudad', null, 'n0', 'u1', ['L', 'R', 0, 4]),
    R('envios', 'ciudad_destino', 'Ciudad', null, 'n0', 'u1', ['L', 'R', 0, 4]),
    R('envios', 'estado', 'EstadoEnvio', null, 'n0', 'u1', ['R', 'L', 1, 1]),
  ],
};

const completo = {
  completo: true, margenIzq: 90, margenSup: 40, margenDer: 50,
  columnas: [
    { tablas: ['configuracion_tarifa', 'tokens_recuperacion', 'perfil_repartidor', 'TipoVehiculo', 'Rol', 'Ciudad'] },
    { tablas: ['usuarios', 'envios'], y: [40, 410] },
    { tablas: ['EstadoEnvio', 'historial_estados', 'asignaciones', 'novedades', 'evidencias_entrega', 'notificaciones', 'ubicaciones_repartidor'] },
    { tablas: ['EstadoAsignacion', 'TipoNovedad', 'TipoEvidencia', 'TipoNotificacion', 'CanalNotificacion', 'EstadoNotificacion'] },
  ],
  gaps: [130, 150, 150],
  relaciones: [
    // usuarios.id desde la izquierda
    R('configuracion_tarifa', 'actualizado_por_id', 'usuarios', 'id', 'n0', 'u0', ['R', 'L', 0, 0]),
    R('tokens_recuperacion', 'usuario_id', 'usuarios', 'id', 'n0', 'u1', ['R', 'L', 0, 0]),
    R('perfil_repartidor', 'usuario_id', 'usuarios', 'id', 'u0', 'u1', ['R', 'L', 0, 0]),
    R('envios', 'emisor_id', 'usuarios', 'id', 'n0', 'u1', ['L', 'L', 0, 0]),
    R('envios', 'repartidor_id', 'usuarios', 'id', 'n0', 'u0', ['L', 'L', 0, 0]),
    R('usuarios', 'rol', 'Rol', null, 'n0', 'u1', ['L', 'R', 0, 2]),
    R('envios', 'ciudad_origen', 'Ciudad', null, 'n0', 'u1', ['L', 'R', 0, 4]),
    R('envios', 'ciudad_destino', 'Ciudad', null, 'n0', 'u1', ['L', 'R', 0, 4]),
    R('perfil_repartidor', 'tipo_vehiculo', 'TipoVehiculo', null, 'n0', 'u1', ['L', 'L', -1, 0]),
    // envios.id y usuarios.id desde la derecha
    R('historial_estados', 'envio_id', 'envios', 'id', 'n0', 'u1', ['L', 'R', 1, 0]),
    R('asignaciones', 'envio_id', 'envios', 'id', 'n0', 'u1', ['L', 'R', 1, 0]),
    R('novedades', 'envio_id', 'envios', 'id', 'n0', 'u1', ['L', 'R', 1, 0]),
    R('evidencias_entrega', 'envio_id', 'envios', 'id', 'n0', 'u1', ['L', 'R', 1, 0]),
    R('notificaciones', 'envio_id', 'envios', 'id', 'n0', 'u1', ['L', 'R', 1, 0]),
    R('ubicaciones_repartidor', 'envio_id', 'envios', 'id', 'n0', 'u0', ['L', 'R', 1, 0]),
    R('envios', 'estado', 'EstadoEnvio', null, 'n0', 'u1', ['R', 'L', 1, 2]),
    R('historial_estados', 'usuario_id', 'usuarios', 'id', 'n0', 'u0', ['L', 'R', 1, 4]),
    R('asignaciones', 'repartidor_id', 'usuarios', 'id', 'n0', 'u1', ['L', 'R', 1, 4]),
    R('asignaciones', 'asignado_por_id', 'usuarios', 'id', 'n0', 'u1', ['L', 'R', 1, 4]),
    R('novedades', 'repartidor_id', 'usuarios', 'id', 'n0', 'u1', ['L', 'R', 1, 4]),
    R('evidencias_entrega', 'repartidor_id', 'usuarios', 'id', 'n0', 'u1', ['L', 'R', 1, 4]),
    R('notificaciones', 'usuario_id', 'usuarios', 'id', 'n0', 'u0', ['L', 'R', 1, 4]),
    R('ubicaciones_repartidor', 'repartidor_id', 'usuarios', 'id', 'n0', 'u1', ['L', 'R', 1, 4]),
    // enums de la derecha
    R('historial_estados', 'estado_anterior', 'EstadoEnvio', null, 'n0', 'u0', ['R', 'R', 2, 0]),
    R('historial_estados', 'estado_nuevo', 'EstadoEnvio', null, 'n0', 'u1', ['R', 'R', 2, 0]),
    R('asignaciones', 'estado', 'EstadoAsignacion', null, 'n0', 'u1', ['R', 'L', 2, 2]),
    R('novedades', 'tipo', 'TipoNovedad', null, 'n0', 'u1', ['R', 'L', 2, 2]),
    R('evidencias_entrega', 'tipo', 'TipoEvidencia', null, 'n0', 'u1', ['R', 'L', 2, 2]),
    R('notificaciones', 'tipo', 'TipoNotificacion', null, 'n0', 'u1', ['R', 'L', 2, 2]),
    R('notificaciones', 'canal', 'CanalNotificacion', null, 'n0', 'u1', ['R', 'L', 2, 4]),
    R('notificaciones', 'estado', 'EstadoNotificacion', null, 'n0', 'u1', ['R', 'L', 2, 3]),
  ],
};

// Alinear los enums de la derecha con la fila que los usa (líneas casi rectas)
function alinearEnums(cfg, ajustes, ci = 3) {
  const pre = construir(cfg);
  const col = cfg.columnas[ci];
  col.y = col.tablas.map((n) => {
    const [tabla, columna, extra] = ajustes[n];
    const tb = pre.t[tabla];
    const i = tb.filas.findIndex((f) => f[1] === columna);
    return tb.y + HEAD + i * ROW + ROW / 2 - HEAD / 2 + (extra || 0);
  });
}
alinearEnums(sprint1, { EstadoEnvio: ['envios', 'estado'] }, 2);
alinearEnums(completo, {
  EstadoAsignacion: ['asignaciones', 'estado'],
  TipoNovedad: ['novedades', 'tipo'],
  TipoEvidencia: ['evidencias_entrega', 'tipo'],
  TipoNotificacion: ['notificaciones', 'tipo', -60],
  CanalNotificacion: ['notificaciones', 'canal', 120],
  EstadoNotificacion: ['notificaciones', 'estado', 230],
});

const diagramas = [['diagrama-er-sprint-1', sprint1], ['diagrama-er', completo]];

if (process.argv.includes('--mermaid')) {
  diagramas.forEach(([nombre, cfg]) => console.log(`%% ${nombre}\n${mermaid(cfg)}`));
  process.exit(0);
}

const NAVEGADORES = [
  process.env.CHROME,
  'C:/Program Files/Google/Chrome/Application/chrome.exe',
  'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  '/usr/bin/google-chrome',
  '/usr/bin/chromium',
].filter(Boolean);
const navegador = NAVEGADORES.find((n) => fs.existsSync(n));
if (!navegador) throw new Error('No encontré Chrome ni Edge. Pasa la ruta en la variable CHROME.');

for (const [nombre, cfg] of diagramas) {
  const { doc, W, H } = construir(cfg);
  const html = path.join(os.tmpdir(), `${nombre}.html`);
  fs.writeFileSync(html,
    `<!doctype html><html><head><meta charset="utf-8"><style>html,body{margin:0;background:#fff}</style></head><body>${doc}</body></html>`);
  execFileSync(navegador, [
    '--headless=new', '--disable-gpu', '--hide-scrollbars', '--force-device-scale-factor=2',
    `--window-size=${Math.ceil(W)},${Math.ceil(H)}`, `--screenshot=${path.join(OUT, `${nombre}.png`)}`,
    `file:///${html.replace(/\\/g, '/')}`,
  ], { stdio: 'ignore' });
  console.log(`${nombre}.png (${Math.ceil(W) * 2} x ${Math.ceil(H) * 2})`);
}
