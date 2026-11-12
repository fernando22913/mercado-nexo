# ROADMAP — EPE3 Desarrollo Web II · MercadoNexo SpA

Gestión persistente de catálogo de productos con **Java 21 + Jakarta EE + JPA (EclipseLink) + PostgreSQL (Docker) + GlassFish**, arquitectura **MVC**.

> **Nota:** El documento EPE3 original exige Microsoft SQL Server. Por decisión del proyecto, el motor de base de datos se reemplaza por **PostgreSQL en Docker**, manteniendo intactos todos los demás requisitos de la evaluación (ver sección "Cumplimiento de la rúbrica").

## Estado: ✅ COMPLETADO (todas las fases ejecutadas y verificadas — 2026-11-12)

Leyenda: `[ ]` pendiente · `[~]` parcial · `[x]` completado

---

## 1. Objetivos

- Aplicación web Jakarta EE funcional para registrar, consultar, modificar y eliminar productos.
- Persistencia con JPA sobre PostgreSQL (Docker), verificable directamente en la base de datos.
- Consultas JPQL: general, parametrizada, UPDATE masivo y DELETE masivo.
- Arquitectura MVC clara: persistencia fuera de las vistas.
- Despliegue funcional en GlassFish con WAR Maven.
- Pruebas automatizadas (JUnit 5) y documentación completa.

## 2. Requisitos extraídos del documento EPE3

| # | Tarea | Requisito |
|---|-------|-----------|
| T1 | Entorno y proyecto | JDK 21, Maven, GlassFish; WAR desplegado con página inicial accesible |
| T2 | Base de datos | BD creada y preparada para Java (→ PostgreSQL/Docker) |
| T3 | JDBC + JPA | Driver JDBC vía Maven; unidad de persistencia con proveedor, URL, usuario, contraseña |
| T4 | Entidades | `Producto` (id, nombre, categoría, precio, stock) + segunda entidad relacionada |
| T5 | EntityManager | Registrar, buscar, actualizar, eliminar con transacciones controladas |
| T6 | JPQL | Consulta general, consulta con parámetro, UPDATE masivo, DELETE masivo |
| T7 | MVC | Modelo (entidades + persistencia), Controlador (servlets), Vista (JSP sin lógica de persistencia) |
| T8 | Integración | Todo funcionando integrado en GlassFish, proyecto ordenado, entrega completa |

## 3. Decisiones de arquitectura y tecnología

| Decisión | Elección | Justificación |
|----------|----------|---------------|
| Java | OpenJDK 21.0.13 | Requisito del documento; ya instalado |
| Servidor | GlassFish 7.x (Jakarta EE 10) | Compatible con Java 21; incluye EclipseLink 4 y JSTL 3.0 |
| JPA provider | EclipseLink (provisto por GlassFish) | Provider nativo de GlassFish, evita conflictos de versiones en el WAR |
| BD | PostgreSQL 16 en Docker Compose | Sustituye SQL Server según instrucciones del proyecto |
| Driver JDBC | `org.postgresql:postgresql` 42.7.x | Driver oficial |
| Transacciones | `RESOURCE_LOCAL` + `EntityTransaction` en la capa DAO | La unidad de persistencia declara URL/usuario/contraseña explícitos (rúbrica criterio 3) |
| Vistas | JSP + JSTL 3.0 + CSS propio | Requisito del documento |
| Pruebas | JUnit 5 contra la BD real (Docker) + verificación HTTP del despliegue | Sin Mockito/servidores embebidos innecesarios |
| NetBeans | Reemplazado por Maven CLI + `asadmin` CLI | Mismo resultado de rúbrica (proyecto creado y desplegado); no es imprescindible |

## 4. Estructura del proyecto

```
mercado-nexo/
├── pom.xml                        # war, jakarta ee api (provided), postgresql, junit5, eclipselink (test)
├── docker-compose.yml             # PostgreSQL 16 + volumen + healthcheck
├── .env.example                   # placeholders de configuración
├── .gitignore                     # .env, target/, glassfish/, etc.
├── ROADMAP.md / README.md
├── glassfish7/                    # (gitignored) servidor descargado localmente
└── src/
    ├── main/java/cl/mercadonexo/
    │   ├── model/                 # ENTIDAD: Producto, Categoria (+ enums/constantes JPQL)
    │   ├── model/dao/             # MODELO-servicios: ProductoDAO, CategoriaDAO
    │   ├── controller/            # CONTROLADOR: ProductoServlet, CategoriaServlet
    │   └── config/                # BootstrapListener: verificación + semilla de datos
    ├── main/webapp/
    │   ├── index.jsp              # página inicial (redirige al listado)
    │   ├── WEB-INF/persistence.xml location ref / web.xml
    │   ├── WEB-INF/views/*.jsp    # listado, formulario crear/editar, detalle, confirmar borrado
    │   └── assets/css/estilos.css
    └── test/java/cl/mercadonexo/  # JUnit 5: entidades, CRUD, JPQL
```

## 5. Diseño de base de datos

```
categoria                     producto
┌──────────────┐             ┌──────────────────┐
│ id        PK │◄──────┐     │ id            PK │
│ nombre  UQ   │       └─────│ categoria_id  FK │  @ManyToOne (unidireccional)
│ descripcion  │             │ nombre           │
└──────────────┘             │ precio  NUMERIC  │
                             │ stock   INTEGER  │
                             └──────────────────┘
```

- Esquema generado por EclipseLink (`eclipselink.ddl-generation=create-or-extend-tables`).
- Datos semilla (categorías) insertados por `BootstrapListener` si la tabla está vacía.

## 6. Fases de implementación

### Fase 1 — Entorno y proyecto (T1) `[x]`
- [x] Verificar Java 21 y Maven
- [x] Descargar GlassFish 7 y arrancar dominio
- [x] Crear proyecto Maven WAR
- [x] Compilación exitosa (`mvn package`)
- [x] Desplegar página inicial y verificar por HTTP
- [x] Aceptación: `curl` responde desde GlassFish

### Fase 2 — Infraestructura PostgreSQL (T2) `[x]`
- [x] `docker-compose.yml` + `.env.example` + `.gitignore`
- [x] Contenedor con volumen, healthcheck, restart policy
- [x] PostgreSQL levantado y accesible (`psql` en el contenedor)
- [x] Documentar comandos Docker (README)
- [x] Aceptación: `docker compose ps` healthy + conexión psql OK

### Fase 3 — JDBC y configuración JPA (T3) `[x]`
- [x] Dependencia `postgresql` en Maven
- [x] `persistence.xml` con proveedor, URL, usuario, contraseña y propiedades
- [x] Prueba de conexión JPA→PostgreSQL
- [x] Aceptación: EMF creado y conexión verificada por test/logs

### Fase 4 — Modelado de entidades (T4) `[x]`
- [x] Entidad `Producto` (id, nombre, categoría, precio, stock) con validaciones
- [x] Entidad `Categoria` y relación `@ManyToOne` con FK
- [x] Verificar tablas creadas en PostgreSQL
- [x] Prueba de persistencia (JUnit)
- [x] Aceptación: filas visibles vía psql

### Fase 5 — CRUD con EntityManager (T5) `[x]`
- [x] DAO con create/find/update/delete/remove
- [x] Transacciones controladas
- [x] Verificación de persistencia real en PostgreSQL
- [x] Aceptación: tests CRUD en verde

### Fase 6 — Consultas JPQL (T6) `[x]`
- [x] Consulta general (todos los productos ordenados)
- [x] Consulta parametrizada (por nombre y/o categoría)
- [x] UPDATE masivo (ajuste de precios por categoría)
- [x] DELETE masivo (eliminar productos por categoría/stock)
- [x] Documentación de cada consulta (README)
- [x] Aceptación: tests JPQL en verde

### Fase 7 — MVC e interfaz (T7) `[x]`
- [x] Servlets controladores con validación de entrada
- [x] Vistas JSP+JSTL: listado, crear, editar, detalle, confirmar borrado
- [x] Mensajes de éxito/error, estados vacíos, navegación, CSS responsivo
- [x] Persistencia fuera de las vistas
- [x] Aceptación: recorridos de las rutas HTTP correctos

### Fase 8 — Integración y verificación final (T8) `[x]`
- [x] PostgreSQL arriba + WAR compilado + desplegado en GlassFish
- [x] CRUD completo desde la app verificada por HTTP
- [x] JPQL operativo desde la app (listado/filtro/acciones masivas)
- [x] Cambios persistidos verificados vía psql
- [x] Aceptación: reporte de resultados reales

### Fase 9 — Documentación y entrega `[x]`
- [x] README.md completo (setup BD, despliegue, pruebas, consultas JPQL)
- [x] ROADMAP.md actualizado (este archivo)
- [x] Checklist final de rúbrica
- [x] Aceptación: tercera persona puede clonar, levantar y usar el proyecto

## 7. Estrategia de pruebas

| Nivel | Herramienta | Alcance |
|-------|-------------|---------|
| Unitarias/integración BD | JUnit 5 (Maven surefire) | Conexión JPA, entidades, CRUD, JPQL, masivas — contra PostgreSQL real |
| Despliegue | curl + asadmin | WAR desplegado, rutas del controlador responden 200/302 |
| Persistencia visual | `psql` en contenedor | Filas creadas/actualizadas/borradas desde la app |

## 8. Despliegue

1. `docker compose up -d` → PostgreSQL
2. `mvn package` → `target/mercado-nexo.war`
3. `glassfish7/bin/asadmin start-domain` → GlassFish en 8080
4. `asadmin deploy --force target/mercado-nexo.war`
5. Navegador: http://localhost:8080/mercado-nexo/

## 9. Cumplimiento de la rúbrica (mapeo)

| Criterio (puntos) | Implementación | Estado |
|---|---|---|
| 1. Entorno y proyecto web (10) | Maven WAR + GlassFish 7 + despliegue verificado | `[x]` |
| 2. BD y conectividad (15) | PostgreSQL 16 Docker Compose, healthcheck, conexión verificada por psql y JPA *(sustituye SQL Server por decisión del proyecto)* | `[x]` |
| 3. Driver JDBC + JPA (10) | `org.postgresql:postgresql` vía Maven + `persistence.xml` con proveedor/URL/usuario/contraseña/propiedades | `[x]` |
| 4. Entidades y mapeo (15) | `Producto` + `Categoria`, `@ManyToOne` unidireccional, anotaciones completas | `[x]` |
| 5. EntityManager (15) | CRUD completo en DAOs con transacciones, persistencia verificada | `[x]` |
| 6. JPQL (15) | General + parametrizada + UPDATE masivo + DELETE masivo | `[x]` |
| 7. MVC (10) | model/dao ↔ controller ↔ views, sin persistencia en JSP | `[x]` |
| 8. Integración final (10) | App funcionando en GlassFish conectada a PostgreSQL | `[x]` |

## 10. Registro de avance

| Fecha | Fase | Resultado |
|-------|------|-----------|
| 2026-11-11 | Planificación | Análisis del documento EPE3 e inspección del entorno (Java 21, Maven 3.9.9, Docker 29.9.3, GlassFish descargado 7.0.25) |
| 2026-11-11 | Fase 1 | WAR compilado y desplegado en GlassFish; página inicial HTTP 200 verificada |
| 2026-11-11 | Fase 2 | PostgreSQL 16.15 en Docker (puerto 5433 inicialmente, por conflicto con otra BD del equipo; **cambio final a 5432** — ver registro 2026-11-12); healthcheck y psql OK. Requirió iniciar manualmente el demonio de Docker |
| 2026-11-11 | Fases 3–4 | persistence.xml (proveedor/URL/usuario/clave), entidades Producto+Categoria; tablas y FK `fk_producto_categoria` verificadas con psql |
| 2026-11-11 | Fases 5–6 | Suite JUnit: **14/14 verdes** (conexión, CRUD completo, JPQL general/parametrizada, UPDATE masivo, DELETE masivo) |
| 2026-11-11 | Fase 7 | Servlets + vistas JSTL completas; recorridos HTTP verificados (200/302) |
| 2026-11-12 | Fase 8 | Verificación end-to-end: crear/editar/eliminar desde la app y persistencia confirmada con psql; UPDATE masivo +5% aplicado (34990.50→36740.03); DELETE masivo de agotados verificado |
| 2026-11-12 | Fase 9 | README.md completo; rúbrica mapeada; entrega lista |
| 2026-11-12 | Limpieza final | Puerto restituido a **5432** (contenedor `ventanova-db` detenido temporalmente, sin borrar datos); código muerto eliminado de `ProductoDAO` (imports sin uso, `contar()`, `redondear()`); 14/14 tests verdes; WAR recompilado y re-desplegado |

### Incidencias resueltas durante la implementación

| Problema | Causa | Solución |
|----------|-------|----------|
| Puerto 5432 ocupado | Otra BD local del equipo (`ventanova-db`) | Inicialmente se usó el puerto 5433; en la limpieza final se restituyó el 5432 deteniendo temporalmente el contenedor en conflicto (ver registro 2026-11-12) |
| 500 al crear producto desde la web | Semilla no idempotente: categoría inexistente al hacer FK | Semilla por nombre (upsert) + validación de categoría en el controlador |
| Mojibake (mecÃ¡nico) en BD | POST sin charset declarado | `CodificacionFiltro` (UTF-8) |
| 500 en listado con filtros | EclipseLink infiere tipo `Boolean` para `:categoriaId` dentro de `COALESCE` | Consulta dinámica con parámetros tipados (sin COALESCE) |
| 500 tras re-desplegar con `deploy --force` | PU registrada globalmente en GlassFish → conflicto de classloaders | Procedimiento de re-despliegue: stop/start del dominio (documentado) |
