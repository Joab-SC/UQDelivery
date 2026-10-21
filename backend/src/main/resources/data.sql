-- ============================================================
-- UQDelivery · datos de prueba
--
-- Se ejecuta cada vez que arranca el back, justo después de crear las tablas
-- (ddl-auto: create-drop), así que la base siempre empieza con estos datos.
--
-- Todos los usuarios tienen la contraseña: Clave123*
-- (abajo va su hash BCrypt, porque la base nunca guarda contraseñas en texto plano).
--
-- Los ids de usuarios y envíos los genera la base; las referencias se buscan por correo.
-- Las tablas usan snake_case: contrasenaHash -> contrasena_hash, emisor -> emisor_id.
-- ============================================================

-- ------------------------------------------------------------
-- Usuarios (uno por rol + un repartidor extra)
-- ------------------------------------------------------------
INSERT INTO usuarios (nombre, correo, contrasena_hash, telefono, rol, activo, notificaciones_activas, fecha_registro) VALUES
  ('Administrador',        'admin@uqdelivery.co',       '$2a$10$bjWByssX9MCdchecx7yJj.k9F1dIrxG/Kk2COpDK.rh42adwCUbu6', '3000000001', 'ADMIN',      true, true, NOW() - INTERVAL '30 days'),
  ('Emisor de prueba',     'emisor@uqdelivery.co',      '$2a$10$bjWByssX9MCdchecx7yJj.k9F1dIrxG/Kk2COpDK.rh42adwCUbu6', '3000000002', 'EMISOR',     true, true, NOW() - INTERVAL '20 days'),
  ('Repartidor de prueba', 'repartidor@uqdelivery.co',  '$2a$10$bjWByssX9MCdchecx7yJj.k9F1dIrxG/Kk2COpDK.rh42adwCUbu6', '3000000003', 'REPARTIDOR', true, true, NOW() - INTERVAL '20 days'),
  ('Carlos Ruiz',          'carlos@uqdelivery.co',      '$2a$10$bjWByssX9MCdchecx7yJj.k9F1dIrxG/Kk2COpDK.rh42adwCUbu6', '3209876543', 'REPARTIDOR', true, true, NOW() - INTERVAL '10 days');

-- ------------------------------------------------------------
-- Configuración de tarifas (una sola fila, id = 1)
-- ------------------------------------------------------------
INSERT INTO configuracion_tarifa (id, tarifa_base, precio_por_km, precio_por_kg, factor_ruta, distancia_minima_km, peso_maximo_kg, medida_maxima_cm, actualizado_por_id, fecha_actualizacion) VALUES
  (1, 5000, 200, 1000, 1.4, 5, 50, 150, NULL, NOW());

-- ------------------------------------------------------------
-- Envíos (uno en cada estado)
-- Distancia y costo calculados con la regla de HU-05:
--   km = max(Haversine entre ciudades × 1.4, 5)
--   costo = 5000 + km × 200 + kg × 1000, redondeado a la centena
-- ------------------------------------------------------------
INSERT INTO envios (codigo_seguimiento, emisor_id, repartidor_id,
                    direccion_origen, ciudad_origen, direccion_destino, ciudad_destino,
                    nombre_destinatario, telefono_destinatario, correo_destinatario, descripcion,
                    peso_kg, largo_cm, ancho_cm, alto_cm, distancia_km, costo, estado,
                    fecha_creacion, fecha_entrega, fecha_actualizacion) VALUES
  ('UQD-7KQ2M9XA',
   (SELECT id FROM usuarios WHERE correo = 'emisor@uqdelivery.co'), NULL,
   'Carrera 14 # 20-35', 'ARMENIA', 'Calle 19 # 8-40', 'PEREIRA',
   'Andrés López', '3151112233', 'andres.lopez@correo.co', 'Documentos en sobre de manila',
   2.5, 35, 25, 5, 43.56, 16200, 'PENDIENTE',
   NOW() - INTERVAL '2 hours', NULL, NOW() - INTERVAL '2 hours'),

  ('UQD-H4TPZ8RC',
   (SELECT id FROM usuarios WHERE correo = 'emisor@uqdelivery.co'),
   (SELECT id FROM usuarios WHERE correo = 'repartidor@uqdelivery.co'),
   'Avenida Bolívar # 1-50', 'ARMENIA', 'Calle 50 # 23-12', 'ARMENIA',
   'María Torres', '3002223344', NULL, 'Caja pequeña con repuestos de celular',
   1, 20, 15, 10, 5.00, 7000, 'ASIGNADO',
   NOW() - INTERVAL '1 day', NULL, NOW() - INTERVAL '20 hours'),

  ('UQD-W6NB3GJD',
   (SELECT id FROM usuarios WHERE correo = 'emisor@uqdelivery.co'),
   (SELECT id FROM usuarios WHERE correo = 'carlos@uqdelivery.co'),
   'Carrera 7 # 18-21', 'PEREIRA', 'Calle 65 # 23-50', 'MANIZALES',
   'Jorge Ramírez', '3123334455', 'jorge.ramirez@correo.co', 'Licuadora en caja original',
   8, 45, 35, 40, 48.49, 22700, 'EN_CAMINO',
   NOW() - INTERVAL '2 days', NULL, NOW() - INTERVAL '3 hours'),

  ('UQD-P2XEV5KM',
   (SELECT id FROM usuarios WHERE correo = 'emisor@uqdelivery.co'),
   (SELECT id FROM usuarios WHERE correo = 'repartidor@uqdelivery.co'),
   'Calle 40 # 25-10', 'CALARCA', 'Carrera 5 # 12-30', 'MONTENEGRO',
   'Sofía Vargas', '3164445566', NULL, 'Libro de regalo envuelto',
   0.8, 25, 18, 4, 17.54, 9300, 'ENTREGADO',
   NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),

  ('UQD-R9FC7TQH',
   (SELECT id FROM usuarios WHERE correo = 'emisor@uqdelivery.co'), NULL,
   'Carrera 19 # 10N-15', 'ARMENIA', 'Avenida Santander # 60-15', 'MANIZALES',
   'Pedro Castaño', '3185556677', 'pedro.castano@correo.co', 'Monitor de 24 pulgadas',
   15, 70, 50, 20, 87.07, 37400, 'CANCELADO',
   NOW() - INTERVAL '3 days', NULL, NOW() - INTERVAL '3 days');
