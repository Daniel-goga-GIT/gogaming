-- No se crean usuarios aquí: este script se ejecuta en cada arranque (también en producción)
-- y unos administradores con contraseña conocida serían una puerta abierta.
-- Para tener un admin: registrarse en la web y ejecutar
--   INSERT INTO user_roles (user_id, role_id) SELECT idusuario, 1 FROM usuarios WHERE correo_electronico = '...';

INSERT IGNORE INTO roles (id, name) VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_USER');

-- Catalogo de productos (IDs explícitos — IDs 13-16 reservados para teclados legacy, sustituidos por 39-49)
INSERT IGNORE INTO productos (idproducto, nombre, marca, fecha_publicacion, precio, stock, tipo_producto, image) VALUES
    (1,  'Beast X Pro', 'WLMouse', '2024-02-10', 139.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433640/34639_1-Photoroom_mc5l4h.png'),
    (2,  'X2H', 'Pulsar', '2023-08-15', 99.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433640/36416_1-Photoroom_uzaubv.png'),
    (3,  'Viper V3 Pro Faker Edition', 'Razer', '2024-05-10', 179.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433640/33661_1-Photoroom_bisgml.png'),
    (4,  'Hyperlight', 'Hitscan', '2024-01-20', 119.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433624/32162_1-Photoroom_k5ayyq.png'),
    (5,  'Maya X', 'Lamzu', '2024-04-05', 109.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433639/32788_1-Photoroom_bzmpzm.png'),
    (6,  'XLite V4', 'Pulsar', '2024-06-01', 99.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433640/34659_1-Photoroom_tdnfz7.png'),
    (7,  'Sora V2', 'Ninjutso', '2024-03-15', 109.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433640/37687_1-Photoroom_n6meua.png'),
    (8,  'G Pro X Superlight 2', 'Logitech', '2023-09-05', 159.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/f_auto,q_auto/28012_1-Photoroom_xyytyt'),
    (9,  'DeathAdder V4', 'Razer', '2024-08-10', 149.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433642/D35264_1-Photoroom_np4txz.png'),
    (24, 'Leviathan V4 Dual 8K', 'RAWM', '2024-11-15', 129.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433641/36539_1-Photoroom_oanqah.png'),
    (25, 'HT-S2 8K', 'G-Wolves', '2024-10-20', 159.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433638/32179_1-Photoroom_zgh9xc.png'),
    (26, 'ULX Frostlord - Classic', 'Finalmouse', '2024-12-01', 189.00, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433641/37830_1-Photoroom_vw75ox.png'),
    (27, 'OP1 8K V2', 'Endgame Gear', '2024-09-10', 79.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433640/35327_1-Photoroom_sllwmx.png'),
    (28, 'ZywOo the Chosen Mouse Gen.2 Medium', 'Pulsar', '2025-01-15', 129.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433643/38626_1-Photoroom_qrotrl.png'),
    (29, 'Huan Stainless Magnesium Alloy', 'WLMouse', '2024-08-05', 149.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433641/37736_1-Photoroom_qqj7de.png'),
    (30, 'Fenris Max 8K', 'G-Wolves', '2025-02-10', 169.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433640/35656_1-Photoroom_nitdce.png'),
    (31, 'Ghost Ultimate Carbon Fiber', 'ATK', '2024-11-30', 139.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433642/38061_1-Photoroom_fcrqrl.png'),
    (32, 'V8 Wireless 8K Superlight', 'Scyrox', '2024-10-05', 99.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433561/31965_1-Photoroom_dtofcv.png'),
    (33, 'StormBreaker Max CF 8K', 'Pwnage', '2025-03-20', 179.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433639/32223_1-Photoroom_rowvi0.png'),
    (34, 'Sword X 8K', 'WLMouse', '2024-12-15', 139.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433553/31599_1-Photoroom_yj4jy7.png'),
    (35, 'ZERO', 'ATK', '2025-01-05', 119.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433642/38997_1-Photoroom_iqkj7e.png'),
    (36, 'HTX Mini 8K', 'G-Wolves', '2025-02-25', 149.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433640/32407_1-Photoroom_tz7wtg.png'),
    (37, 'G Pro X2 Superstrike', 'Logitech', '2026-02-10', 179.99, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433641/37854_1-Photoroom_g0z2db.png'),
    (38, 'WLmouse Ying Magnesium', 'WLmouse', '2026-03-10', 149.90, 20, 'Raton', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1776433642/38721_1-Photoroom_slvus1.png');

-- Especificaciones: Ratones
INSERT IGNORE INTO raton (idproducto, tipo_agarre, medidas, sensor, peso, switches) VALUES
    (1, 'Fingertip', '122x62x37mm', 'PAW3950', 39.0, 'Omron Optical'),
    (2, 'Claw', '120x65x39mm', 'PAW3395', 54.0, 'Optical'),
    (3, 'Fingertip', '127x62x39mm', 'Focus Pro 35K', 54.0, 'Razer Optical Gen-3'),
    (4, 'Claw', '120x58x38mm', 'PAW3395', 39.0, 'Huano Blue Shell Pink Dot'),
    (5, 'Claw', '124x64x39mm', 'PAW3395', 45.0, 'Omron Optical'),
    (6, 'Palm', '122x66x43mm', 'PAW3395', 50.0, 'Optical'),
    (7, 'Claw', '119x59x37mm', 'PAW3395', 39.0, 'Omron Optical'),
    (8, 'Palm', '125x63x40mm', 'HERO 2', 60.0, 'Lightforce'),
    (9, 'Palm', '128x68x44mm', 'Focus Pro 35K', 63.0, 'Razer Optical Gen-3'),
    (24, 'Claw', '120x62x38mm', 'PAW3950', 42.0, 'Omron Optical'),
    (25, 'Fingertip', '113x58x36mm', 'PAW3950', 36.0, 'Zippy'),
    (26, 'Fingertip', '121x58x37mm', 'PAW3395', 35.0, 'Omron Optical'),
    (27, 'Claw', '118x60x38mm', 'PAW3395', 50.0, 'Kailh GX'),
    (28, 'Claw', '120x65x39mm', 'PAW3395', 53.0, 'Optical'),
    (29, 'Claw', '122x62x38mm', 'PAW3950', 41.0, 'Omron Optical'),
    (30, 'Palm', '125x65x41mm', 'PAW3950', 48.0, 'Omron'),
    (31, 'Claw', '121x61x38mm', 'PAW3950', 38.0, 'Omron Optical'),
    (32, 'Claw', '118x60x38mm', 'PAW3950', 36.0, 'Omron Optical'),
    (33, 'Palm', '124x66x42mm', 'PAW3395', 49.0, 'Omron'),
    (34, 'Claw', '123x64x39mm', 'PAW3950', 47.0, 'Omron Optical'),
    (35, 'Fingertip', '115x58x35mm', 'PAW3950', 34.0, 'Omron Optical'),
    (36, 'Fingertip', '110x55x34mm', 'PAW3395', 32.0, 'Zippy'),
    (37, 'Palm', '125x63x40mm', 'HERO 2', 61.0, 'HITS (Haptic Inductive)'),
    (38, 'Claw', '123x67x38mm', 'PAW3950', 47.0, 'Omron Optical');

-- Limpiar skates anteriores (IDs 10-12 del seed antiguo)
DELETE FROM skate WHERE idproducto IN (10, 11, 12);
DELETE FROM productos WHERE idproducto IN (10, 11, 12);

-- Nuevos skates: productos
INSERT IGNORE INTO productos (idproducto, nombre, marca, fecha_publicacion, precio, stock, tipo_producto, image) VALUES
    (50, 'Jade DIY Universal Donuts', 'X-raypad', '2024-03-10', 9.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280971/X-raypad_Jade_DIY_Mouse_Skates_-_Universal_Donuts_iymvj2.png'),
    (51, 'Obsidian Air Pro Universal Donuts', 'X-raypad', '2024-05-20', 12.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280971/X-raypad_Obsidian_Air_Pro_Mouse_Skates_-_Universal_Donuts_xi62xj.png'),
    (52, 'Jade Air Universal Dots', 'X-raypad', '2024-04-01', 9.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280971/X-raypad_Jade_Air_Mouse_Skates_-_Universal_Dots_jtt9g4.png'),
    (53, 'Edgerunner Cyclone Universal PTFE Dots 40pcs', 'GHOSTGLIDES', '2024-07-15', 14.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280972/GHOSTGLIDES_Edgerunner_Cyclone_Mouse_Skates_-_Universal_PTFE_Dots_40pcs_nvaiij.png'),
    (54, 'U9 Air 6.5mm Dot Skates', 'X-raypad', '2024-06-10', 11.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280972/X-raypad_U9_Air_6.5mm_-_Dot_Skates_k02gki.png'),
    (55, 'Obsidian Air Universal Dots', 'X-raypad', '2024-02-25', 9.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280972/X-raypad_Obsidian_Air_Mouse_Skates_-_Universal_Dots_k9ju9k.png'),
    (56, 'Obsidian Air Pro Universal Dots', 'X-raypad', '2024-05-20', 12.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280972/X-raypad_Obsidian_Air_Pro_Mouse_Skates_-_Universal_Dots_tgylxy.png'),
    (57, 'Obsidian 6.5mm Universal Dots 40pcs', 'X-raypad', '2024-08-01', 13.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280972/X-raypad_Obsidian_Mouse_Skates_Universal_6.5mm_Dots_-_40pcs-Photoroom_gfud6e.png'),
    (58, 'Jade 6.5mm Universal Dots 40pcs', 'X-raypad', '2024-08-01', 13.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280972/X-raypad_Jade_Mouse_Skates_Universal_6.5mm_Dots_-_40pcs-Photoroom_d5ifzj.png'),
    (59, 'MIZUGUMO FUTAE P8', 'Artisan', '2024-09-05', 19.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280973/Artisan_MIZUGUMO_FUTAE_P8_Mouse_Skates-Photoroom_btogyc.png'),
    (60, 'Cyclone Pro Universal Dots 40pcs', 'GHOSTGLIDES', '2024-07-15', 14.99, 20, 'Skate', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779280973/GHOSTGLIDES_Cyclone_Pro_-_Universal_Mouse_Skates_-_Dots_40pcs-Photoroom_vzuj3y.png');

-- Especificaciones: Skates (pies de raton)
INSERT IGNORE INTO skate (idproducto, material, grosor, compatibilidad) VALUES
    (50, 'PTFE Jade', 0.60, 'Universal'),
    (51, 'PTFE Obsidian', 0.45, 'Universal'),
    (52, 'PTFE Jade', 0.45, 'Universal'),
    (53, 'PTFE', 0.60, 'Universal'),
    (54, 'PTFE', 0.45, 'Universal'),
    (55, 'PTFE Obsidian', 0.45, 'Universal'),
    (56, 'PTFE Obsidian', 0.45, 'Universal'),
    (57, 'PTFE Obsidian', 0.60, 'Universal'),
    (58, 'PTFE Jade', 0.60, 'Universal'),
    (59, 'PTFE', 0.70, 'Universal'),
    (60, 'PTFE', 0.60, 'Universal');

-- Limpiar teclados anteriores (IDs 13-16 del seed antiguo)
DELETE FROM teclado WHERE idproducto IN (13, 14, 15, 16);
DELETE FROM productos WHERE idproducto IN (13, 14, 15, 16);

-- Nuevos teclados: productos
INSERT IGNORE INTO productos (idproducto, nombre, marca, fecha_publicacion, precio, stock, tipo_producto, image) VALUES
    (39, 'Zenblade 65 V2 Nyomi Purple Edition', 'Pwnage', '2024-09-01', 149.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277154/Pwnage_Zenblade_65_V2_Hall_Effect_Gaming_Keyboard_Nyomi_Purple_Edition_Purple-Photoroom_kdmdnp.png'),
    (40, 'FORT 75 Limited Edition Magnetic - Pink', 'Gateron x Sikakeyb', '2024-08-01', 139.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277153/Gateron_x_Sikakeyb_FORT_75_Limited_Edition_Magnetic_Keyboard_-_Pink-Photoroom_sxrqxr.png'),
    (41, 'ALU81 RGB Nova [g3ms Moonstone] - Black', 'Dark Project', '2024-07-15', 159.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277157/DARK_PROJECT_ALU81_Gaming_Keyboard_RGB_-_Nova_g3ms_Moonstone_-_Black-Photoroom_ovgezj.png'),
    (42, 'FORT 75 Limited Edition Magnetic - Green', 'Gateron x Sikakeyb', '2024-08-01', 139.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277160/Gateron_x_Sikakeyb_FORT_75_Limited_Edition_Magnetic_Keyboard_-_Green-Photoroom_jefm6z.png'),
    (43, 'HE Magnetic 65% ANSI Katana Edition', 'Yuki Aim', '2024-10-01', 129.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277165/Yuki_Aim_Hall_Effect_Magnetic_65_Gaming_Keyboard_ANSI_-_Katana_Edition-Photoroom_p2sxsl.png'),
    (44, 'Aurora 65 HE - Copper/Black', 'IPI', '2024-06-20', 119.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277168/IPI_Aurora_65_HE_Keyboard_-_Copper-Black-Photoroom_mxqplg.png'),
    (45, 'YING75 HE Carbon Fiber - White [TTC Shadow]', 'WLMouse', '2024-11-10', 169.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277167/WLMouse_YING75_HE_Forged_Carbon_Fiber_Keyboard_-_White_TTC_Shadow_-Photoroom_rssvel.png'),
    (46, 'W60 Kitsune Limited Edition [Hall Effect]', 'Wraith', '2024-09-15', 159.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277164/Wraith_W60_Gaming_Keyboard_-_Kitsune_Limited_Edition_Hall_Effect_-Photoroom_ijgo6c.png'),
    (47, 'RS7 PRO eSports Drive to Win - White', 'ATK', '2024-12-01', 149.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277167/ATK_RS7_PRO_eSports_Hall_Effect_Keyboard_Drive_to_Win_RGB_-_White-Photoroom_i5vuej.png'),
    (48, 'Apex Basecamp Hotswap ISO Nordic - Wraith', 'Higround', '2024-10-20', 179.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277166/Higround_Apex_Basecamp_Hotswap_Gaming_Keyboard_ISO_Nordic_-_Wraith-Photoroom_b1zyeh.png'),
    (49, 'ALU87 RGB Celestial [g3ms Voidstone]', 'Dark Project', '2024-08-25', 169.99, 20, 'Teclado', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779277164/Dark_Project_ALU87_Gaming_Keyboard_RGB_-_Celestial_g3ms_Voidstone_-Photoroom_r0g7j3.png');

-- Especificaciones: Teclados
INSERT IGNORE INTO teclado (idproducto, tipo_switch, layout, conectividad, retroiluminacion) VALUES
    (39, 'Hall Effect Magnetic', '65% ANSI', 'USB-C', 'RGB'),
    (40, 'Gateron Magnetic Hall Effect', '75%', 'USB-C / Wireless', 'RGB'),
    (41, 'g3ms Moonstone', '80% ANSI', 'USB-C', 'RGB'),
    (42, 'Gateron Magnetic Hall Effect', '75%', 'USB-C / Wireless', 'RGB'),
    (43, 'Hall Effect Magnetic', '65% ANSI', 'USB-C', 'RGB'),
    (44, 'Hall Effect Magnetic', '65% ANSI', 'USB-C', 'RGB'),
    (45, 'TTC Shadow Hall Effect', '75%', 'USB-C', 'RGB'),
    (46, 'Hall Effect Magnetic', '60%', 'USB-C', 'RGB'),
    (47, 'Hall Effect Magnetic', 'TKL ANSI', 'USB-C', 'RGB'),
    (48, 'Hotswap Compatible', 'ISO Nordic', 'USB-C / Bluetooth', 'RGB'),
    (49, 'g3ms Voidstone', 'TKL ANSI', 'USB-C', 'RGB');

-- Limpiar alfombrillas anteriores (IDs 17-20 del seed antiguo)
DELETE FROM alfombrilla WHERE idproducto IN (17, 18, 19, 20);
DELETE FROM productos WHERE idproducto IN (17, 18, 19, 20);

-- Nuevas alfombrillas: productos
INSERT IGNORE INTO productos (idproducto, nombre, marca, fecha_publicacion, precio, stock, tipo_producto, image) VALUES
    (61, 'G-SR-SE Rouge II', 'ZOWIE by BenQ', '2024-01-15', 34.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282168/ZOWIE_by_BenQ_G-SR-SE_Rouge_II_Mousepad-Photoroom_p4qhjy.png'),
    (62, 'Rizz SQ Mid - Doodles NachoCustomz Edition', 'La Onda', '2024-06-10', 49.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282167/La_Onda_Rizz_-_Gaming_Mousepad_-_SQ_-_Mid_-_Doodles_-_NachoCustomz_Edition-Photoroom_ufammm.png'),
    (63, 'Temple of Dreams XL', 'Padsmith', '2024-05-20', 59.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282166/Padsmith_Temple_of_Dreams_Gaming_Mousepad_-_XL-Photoroom_g4fgkg.png'),
    (64, 'FX Zero Soft XL - Daidai Orange', 'Artisan', '2023-09-01', 79.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282166/Artisan_Mousepad_-_FX_Zero_-_Soft_-_XL_-_Daidai_Orange-Photoroom_shxq0i.png'),
    (65, 'Samurai Premium', 'Kurosun', '2024-03-15', 44.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282166/Kurosun_Samurai_-_Premium_Gaming_Mousepad-Photoroom_ag82by.png'),
    (66, 'FX Hien Soft XL - Navy Blue', 'Artisan', '2023-09-01', 79.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282165/Artisan_Mousepad_-_FX_Hien_-_SOFT_-_XL_-_Navy_Blue-Photoroom_u4ubst.png'),
    (67, 'Aqua Control II - Sakura Green XL', 'X-raypad', '2024-02-10', 39.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282165/X-raypad_Aqua_Control_II_Mousepad_-_Sakura_Green_-_XL-Photoroom_efyz4m.png'),
    (68, 'Aqua Control Plus - Wave Grass XL', 'X-raypad', '2024-04-01', 44.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282165/X-raypad_Aqua_Control_Plus_Mousepad_-_Wave_-_Grass_-_XL-Photoroom_gy3zai.png'),
    (69, 'Aqua Control Zero - Black XL', 'X-raypad', '2024-04-01', 39.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282164/X-raypad_Aqua_Control_Zero_Mousepad_-_Black_-_XL-Photoroom_jz93r0.png'),
    (70, 'Saturn PRO XL Soft - Red', 'Lethal Gaming Gear', '2024-01-20', 34.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282164/Lethal_Gaming_Gear_Saturn_PRO_Gaming_Mousepad_-_XL_-_Soft_-_Red-Photoroom_oepbun.png'),
    (71, 'eS PD170 XL SQ 500x500 - eS Blue', 'Pulsar', '2024-07-05', 49.99, 20, 'Alfombrilla', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779282164/Pulsar_eS_PD_170_Mousepad_-_XL_SQ_-_500x500_-_eS_blue-Photoroom_lwoj30.png');

-- Especificaciones: Alfombrillas
INSERT IGNORE INTO alfombrilla (idproducto, dimensiones, grosor, superficie) VALUES
    (61, '470x390mm', 3.50, 'Cloth'),
    (62, '450x450mm', 3.00, 'Cloth'),
    (63, '900x400mm', 3.00, 'Cloth'),
    (64, '480x400mm', 3.00, 'Cloth'),
    (65, '900x400mm', 4.00, 'Cloth'),
    (66, '480x400mm', 3.00, 'Cloth'),
    (67, '900x400mm', 4.00, 'Cloth'),
    (68, '900x400mm', 4.00, 'Cloth'),
    (69, '900x400mm', 4.00, 'Cloth'),
    (70, '900x400mm', 3.00, 'Cloth'),
    (71, '500x500mm', 3.00, 'Cloth');

-- Limpiar auriculares anteriores (IDs 21-23 del seed antiguo)
DELETE FROM auriculares WHERE idproducto IN (21, 22, 23);
DELETE FROM productos WHERE idproducto IN (21, 22, 23);

-- Nuevos auriculares: productos
INSERT IGNORE INTO productos (idproducto, nombre, marca, fecha_publicacion, precio, stock, tipo_producto, image) VALUES
    (72, 'Forteza IEM - Blue', 'Kiwi Ears', '2024-08-10', 79.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283240/Kiwi_Ears_Forteza_IEM_Headphones_-_Blue-Photoroom_gkz0ah.png'),
    (73, 'EW300 IEM - 3.5mm', 'SIMGOT', '2024-03-01', 24.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283239/SIMGOT_EW300_IEM_Headphones_-_3.5mm-Photoroom_alruuj.png'),
    (74, 'Zero IEM - Red', 'Truthear', '2023-06-15', 49.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283239/Truthear_Zero_IEM_Headphones_-_Red-Photoroom_jpro3g.png'),
    (75, 'Wan''er S.G IEM - Black', 'Tangzu', '2023-09-20', 19.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283238/Tangzu_Wan_er_S.G_IEM_Headphones_with_3.5mm_Microphone_-_Black-Photoroom_i5cmsp.png'),
    (76, 'Zero 2 IEM - Silver', '7HZ', '2024-04-10', 49.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283237/7HZ_Zero_2_IEM_Headphones_-_Silver-Photoroom_ffxb0q.png'),
    (77, 'Starfield 2 IEM - Purple', 'MoonDrop', '2024-06-01', 99.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283236/MoonDrop_Starfield_2_IEM_Headphones_-_Purple-Photoroom_yoelpo.png'),
    (78, 'Hexa IEM', 'Truthear', '2023-10-05', 79.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283236/Truthear_Hexa_IEM_Headphones-Photoroom_yxxda2.png'),
    (79, 'Zero Blue 2 IEM - 3.5mm Mic', 'Truthear', '2024-05-20', 49.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283236/Truthear_Zero_Blue_2_IEM_Headphones_with_3.5_mm_mic-Photoroom_pmncmh.png'),
    (80, 'CHU 2 IEM', 'MoonDrop', '2024-02-15', 29.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283235/MoonDrop_CHU_2_IEM_Headphones-Photoroom_kxkovj.png'),
    (81, 'EM6L IEM', 'SIMGOT', '2024-07-01', 99.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283235/SIMGOT_EM6L_IEM_Headphones-Photoroom_mrh0la.png'),
    (82, 'Venus Hybrid 3BA+1DD IEM', 'Melody Wings', '2024-09-10', 109.99, 20, 'Auriculares', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283235/Melody_Wings_Venus_Hybrid_3BA1DD_IEM-Photoroom_orzqxi.png');

-- Especificaciones: Auriculares
INSERT IGNORE INTO auriculares (idproducto, tipo, driver, conectividad, impedancia) VALUES
    (72, 'IEM', '14.5mm Planar', '3.5mm Jack', '14Ω'),
    (73, 'IEM', '10mm Dynamic', '3.5mm Jack', '32Ω'),
    (74, 'IEM', 'Dual Dynamic 6mm+10mm', '3.5mm Jack', '18Ω'),
    (75, 'IEM', '10mm Dynamic', '3.5mm Jack + Mic', '32Ω'),
    (76, 'IEM', '1DD+4BA Hybrid', '3.5mm Jack', '24Ω'),
    (77, 'IEM', '10mm Dynamic', '3.5mm Jack', '32Ω'),
    (78, 'IEM', '1DD+3BA Hybrid', '3.5mm Jack', '18Ω'),
    (79, 'IEM', 'Dual Dynamic 6mm+10mm', '3.5mm Jack + Mic', '18Ω'),
    (80, 'IEM', '10mm Dynamic', '3.5mm Jack', '28Ω'),
    (81, 'IEM', '1DD+2BA Hybrid', '3.5mm Jack', '9Ω'),
    (82, 'IEM', '3BA+1DD Hybrid', '3.5mm Jack', '14Ω');

-- Mangas: productos
INSERT IGNORE INTO productos (idproducto, nombre, marca, fecha_publicacion, precio, stock, tipo_producto, image) VALUES
    (83, 'Gaming Sleeve Sakura - Purple L', 'NRV', '2024-03-10', 24.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283973/NRV_Gaming_Sleeve_Sakura_-_Purple_-_L-Photoroom_fuk3xe.png'),
    (84, 'Gaming Sleeve - Blood Orchid S', 'NRV', '2024-03-10', 24.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283973/NRV_Gaming_Sleeve_-_Blood_Orchid_-_S-Photoroom_vtyvz8.png'),
    (85, 'Aim Sleeve V2 - Dusk S', '4114', '2024-05-15', 29.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283972/4114_Aim_Sleeve_V2_-_Dusk_-_Small-Photoroom_qpqxhm.png'),
    (86, 'Gaming Sleeve - Heroic L', 'NRV', '2024-04-01', 24.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283971/NRV_Gaming_Sleeve_-_Heroic_-_Manga_L-Photoroom_mi73gs.png'),
    (87, 'Gaming Sleeve Dark Sakura - White M', 'NRV', '2024-03-10', 24.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283970/NRV_Gaming_Sleeve_Dark_Sakura_-_White_-_M-Photoroom_hax4ns.png'),
    (88, 'Gaming Sleeve - Samurai M', 'NRV', '2024-02-20', 24.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283969/NRV_Gaming_Sleeve_-_Samurai_-_M-Photoroom_wgfnib.png'),
    (89, 'Gaming Sleeve Dark Sakura - Purple M', 'NRV', '2024-03-10', 24.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283968/NRV_Gaming_Sleeve_Dark_Sakura_-_Purple_-_M-Photoroom_ezo3mx.png'),
    (90, 'ES Arm Sleeve Palm Long - White XL', 'Pulsar', '2024-06-20', 34.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283968/Pulsar_ES_Arm_Sleeve_Palm_Long_-_Size_XL_-_White-Photoroom_jo2m4e.png'),
    (91, 'Gaming Sleeve - Dragon L', 'NRV', '2024-02-20', 24.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283967/NRV_Gaming_Sleeve_-_Dragon_-_L-Photoroom_lncici.png'),
    (92, 'x AimLab Limited Edition Arm Sleeve - Tattoo S', 'FOCUS', '2024-08-01', 34.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283967/FOCUS_x_AimLab_Limited_Edition_Arm_Gaming_Sleeve_-_Tattoo_-_S-Photoroom_jezayz.png'),
    (93, 'x AimLab Limited Edition Arm Sleeve - Smoke S', 'FOCUS', '2024-08-01', 34.99, 20, 'Manga', 'https://res.cloudinary.com/dcl5dstmj/image/upload/v1779283966/FOCUS_x_AimLab_Limited_Edition_Arm_Gaming_Sleeve_-_Smoke_-_S-Photoroom_aytdmt.png');

-- Especificaciones: Mangas
INSERT IGNORE INTO manga (idproducto, talla, material, largo, color) VALUES
    (83, 'L',  'Nylon/Lycra', 38.00, 'Purple'),
    (84, 'S',  'Nylon/Lycra', 30.00, 'Red/Black'),
    (85, 'S',  'Nylon/Lycra', 30.00, 'Dusk'),
    (86, 'L',  'Nylon/Lycra', 38.00, 'Yellow/Black'),
    (87, 'M',  'Nylon/Lycra', 34.00, 'White'),
    (88, 'M',  'Nylon/Lycra', 34.00, 'Black/Red'),
    (89, 'M',  'Nylon/Lycra', 34.00, 'Purple'),
    (90, 'XL', 'Nylon/Lycra', 42.00, 'White'),
    (91, 'L',  'Nylon/Lycra', 38.00, 'Black/Red'),
    (92, 'S',  'Nylon/Lycra', 30.00, 'Black'),
    (93, 'S',  'Nylon/Lycra', 30.00, 'Grey');
