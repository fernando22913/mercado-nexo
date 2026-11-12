# MercadoNexo SpA — Sistema de Gestión de Productos

Proyecto **EPE3 — Desarrollo Web II (IPC Chile)**. Aplicación web Jakarta EE para gestionar
el catálogo de productos de MercadoNexo SpA con persistencia real, consultas JPQL y
arquitectura MVC, desplegada en **GlassFish** sobre **PostgreSQL en Docker**.

> **Nota sobre el motor de base de datos:** el documento de evaluación original indica
> Microsoft SQL Server. Por decisión del proyecto se usa **PostgreSQL 16 en Docker**,
> manteniendo intactos todos los demás requisitos (JDBC vía Maven, unidad de persistencia
> con proveedor/URL/usuario/contraseña, JPA, JPQL, MVC, GlassFish).

---

## 1. Stack tecnológico

| Componente | Versión / elección |
|---|---|
| Java | OpenJDK 21 |
| Plataforma | Jakarta EE 10 (GlassFish **7.0.25**) |
| Empaquetado | WAR con Maven 3.9 |
| JPA | EclipseLink 4 (incluido en GlassFish) — `RESOURCE_LOCAL` |
| Base de datos | PostgreSQL **16.15** en Docker Compose (puerto **5432**) |
| Driver JDBC | `org.postgresql:postgresql` 42.7.3 (vía Maven) |
| Vistas | JSP + JSTL 3.0 + CSS responsivo |
| Pruebas | JUnit 5 (14 pruebas contra la BD real) |

## 2. Estructura del proyecto (MVC)

```
mercado-nexo/
├── pom.xml                          # Maven WAR + dependencias
├── docker-compose.yml / .env.example
├── ROADMAP.md                       # Plan + checklist de la rúbrica
└── src/
    ├── main/java/cl/mercadonexo/
    │   ├── model/                   # MODELO — entidades JPA
    │   │   ├── Producto.java        #   id, nombre, categoría (FK), precio, stock
    │   │   └── Categoria.java       #   segunda entidad (relación @ManyToOne)
    │   ├── model/dao/               # MODELO — persistencia (EntityManager)
    │   │   ├── ConexionJPA.java     #   EntityManagerFactory (unidad mercadonexoPU)
    │   │   ├── ProductoDAO.java     #   CRUD + JPQL (general/param./masivas)
    │   │   └── CategoriaDAO.java
    │   ├── controller/              # CONTROLADOR — servlets + validación
    │   │   ├── ProductoServlet.java #   /productos (listar, crear, editar, borrar, masivas)
    │   │   └── CategoriaServlet.java#   /categorias
    │   └── config/
    │       ├── BootstrapListener.java  # semilla idempotente de categorías
    │       └── CodificacionFiltro.java # UTF-8 en formularios
    ├── main/resources/META-INF/persistence.xml
    ├── main/webapp/                 # VISTA — JSP + JSTL (sin persistencia)
    │   ├── index.jsp
    │   ├── assets/css/estilos.css
    │   └── WEB-INF/views/{productos,categorias,error,includes}/
    └── test/java/cl/mercadonexo/    # Pruebas JUnit 5 (BD real)
```

## 3. Base de datos

```
categoria                        producto
┌───────────────┐               ┌──────────────────┐
│ id        PK  │◄──────────┐   │ id            PK │
│ nombre   UQ   │           └───│ categoria_id  FK │  fk_producto_categoria
│ descripcion   │               │ nombre           │
└───────────────┘               │ precio NUMERIC   │
                                │ stock   INTEGER  │
                                └──────────────────┘
```

- El esquema se genera automáticamente con `eclipselink.ddl-generation=create-or-extend-tables`
  al iniciar la aplicación.
- Las 4 categorías base se insertan de forma idempotente por `BootstrapListener`
  (solo se crean si no existen por nombre).

## 4. Puesta en marcha paso a paso

### 4.0 Requisitos previos

| Requisito | Versión mínima / verificación |
|---|---|
| JDK | **Java 21** (`java -version`) |
| Maven | 3.9+ (`mvn -version`) |
| Docker + Docker Compose | cualquier versión reciente con `docker compose` (`docker compose version`) |
| GlassFish 7 | probado con **7.0.25** (descargable de glassfish.org; no se versiona en este repo) |

> **Acerca de este repositorio:** GitHub aloja **únicamente el código fuente y la
> configuración** del proyecto. La base de datos **no** vive en GitHub ni en la nube:
> corre localmente en Docker (contenedor `mercadonexo-db`), por lo que cualquiera que
> clone este repo puede reproducirla con los pasos de esta sección.

### 4.1 PostgreSQL con Docker

```bash
cd mercado-nexo

# Configuración (ya incluye .env con puerto 5432; si otra BD local usa 5432, ajustar .env y persistence.xml)
cp .env.example .env        # opcional: ajustar valores

docker compose up -d        # iniciar PostgreSQL
docker compose ps           # verificar estado (healthy)
docker compose down         # detener (los datos SE CONSERVAN en el volumen nombrado)
docker compose down -v      # detener Y borrar los datos (reset total) — ¡usar con cuidado!
```

**Detener y reiniciar sin perder datos:** los datos viven en el volumen Docker nombrado
`mercadonexo_pgdata`, que `down` y `stop` nunca borran:

```bash
docker compose stop db      # detener el contenedor (volumen intacto)
docker compose start db     # retomar con todos los datos
# o bien: docker compose down && docker compose up -d   (mismo volumen, datos intactos)
# eliminar el volumen SOLO si se quiere resetear: docker compose down -v
```

Acceder con psql:

```bash
docker compose exec db psql -U mercadonexo -d mercadonexo
# consultas útiles:
#   \dt                                  -- listar tablas
#   SELECT * FROM producto;
#   SELECT * FROM categoria;
```

Conexión desde Java (ya configurada en `persistence.xml`):

```
URL:      jdbc:postgresql://localhost:5432/mercadonexo
Usuario:  mercadonexo
Clave:    mercadonexo
```

### 4.2 GlassFish

El servidor se descarga localmente (carpeta `glassfish7/`, ignorada por git):

```bash
# Descargar GlassFish 7.0.25 (solo una vez)
curl -fLO https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/7.0.25/glassfish-7.0.25.zip
unzip glassfish-7.0.25.zip -d .        # crea glassfish7/
```

Arrancar / detener / desplegar:

```bash
glassfish7/bin/asadmin start-domain     # http://localhost:8080 · admin 4848
glassfish7/bin/asadmin deploy target/mercado-nexo.war
glassfish7/bin/asadmin stop-domain
```

**Importante (procedimiento de re-despliegue):** para actualizar la aplicación ya
desplegada, reinicie el dominio en lugar de usar `deploy --force` — GlassFish registra
la unidad de persistencia a nivel de servidor y un re-despliegue en caliente puede
provocar conflictos de classloaders:

```bash
mvn package -DskipTests
glassfish7/bin/asadmin stop-domain && glassfish7/bin/asadmin start-domain
# La aplicación se despliega automáticamente al arrancar
```

### 4.3 Compilar y probar

```bash
mvn package          # compila + empaqueta target/mercado-nexo.war
mvn test             # ejecuta las 14 pruebas JUnit (requiere PostgreSQL arriba)
```

### 4.4 Usar la aplicación

- **Listado:** http://localhost:8080/mercado-nexo/productos
- Crear → botón “+ Nuevo producto”; Editar → botón “Editar”; Detalle → “Ver”;
  Eliminar → “Eliminar” (pide confirmación).
- **Operaciones masivas (JPQL):** panel en el listado — ajuste de precios por
  categoría (UPDATE masivo) y eliminación de productos sin stock (DELETE masivo).
- **Categorías:** http://localhost:8080/mercado-nexo/categorias

## 5. Consultas JPQL implementadas (Tarea 6)

Todas en `ProductoDAO` (`src/main/java/cl/mercadonexo/model/dao/ProductoDAO.java`):

1. **Consulta general** — `Producto.listar`:
   `SELECT p FROM Producto p ORDER BY p.id`
2. **Consulta con condición/parámetros** — `filtrar(nombre, categoriaId)`:
   `SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND p.categoria.id = :categoriaId ORDER BY p.id`
   (las condiciones se agregan dinámicamente según los filtros; los valores siempre
   van por `setParameter`, nunca concatenados).
3. **UPDATE masivo** — `actualizarPreciosPorCategoria(categoriaId, porcentaje)`:
   `UPDATE Producto p SET p.precio = p.precio * (1 + :porcentaje / 100) WHERE p.categoria.id = :categoriaId`
   Operación bulk: transacción propia y `em.clear()` para que futuras lecturas
   vean los valores nuevos desde la BD.
4. **DELETE masivo** — `eliminarPorCategoria(categoriaId)` y `eliminarAgotados()`:
   `DELETE FROM Producto p WHERE p.categoria.id = :categoriaId`
   `DELETE FROM Producto p WHERE p.stock = 0`
   Mismo manejo de transacción/sincronización que el UPDATE masivo.

## 6. Pruebas automatizadas (`mvn test`)

| Clase | Qué verifica | Pruebas |
|---|---|---|
| `ConexionJPATest` | EMF + conexión real a PostgreSQL | 2 |
| `ProductoCRUDTest` | Registrar → buscar → actualizar → listar → eliminar, re-leyendo desde la BD (incluye la relación `@ManyToOne`) | 5 |
| `ProductoJPQLTest` | Consulta general, parametrizada (nombre/categoría/combinada), UPDATE masivo (+10% verificado), DELETE masivo por categoría y de agotados | 7 |

**Última ejecución (2026-11-12): `Tests run: 14, Failures: 0, Errors: 0` — BUILD SUCCESS.**

## 7. Verificación del despliegue (resultados reales)

- `GET /mercado-nexo/` → 200 · `/productos` → 200 · `/categorias` → 200 · CSS → 200
- Crear producto → 302 (redirige al detalle) · fila visible en `psql`
- Editar → 302 · cambios visibles en `psql` (nombre, precio, stock)
- Validaciones (nombre vacío, precio no numérico, stock negativo) → mensajes en el formulario
- UPDATE masivo +5% → `34990.50 → 36740.03` verificado en `psql`
- DELETE masivo de agotados → fila eliminada verificada en `psql`
- Categoría duplicada → error de validación; categoría con productos → no se puede eliminar
- Textos con acentos persisten correctamente (UTF-8, `CodificacionFiltro`)

## 8. Problemas conocidos / limitaciones

- **Puerto 5432:** es el puerto estándar de PostgreSQL y el que usa el proyecto
  (configurable en `.env` — si se cambia, actualizar también `persistence.xml`).
  Si otro proyecto local publica el 5432, detenga temporalmente ese contenedor
  (`docker stop <nombre>`) para poder levantar el de MercadoNexo.
- **Re-despliegue:** ver procedimiento en §4.2 (stop/start del dominio).
- En cada arranque del dominio, EclipseLink puede registrar advertencias de DDL
  (`... already exists`) provenientes de `create-or-extend-tables`: son inofensivas
  (el esquema ya existe y la aplicación continúa normalmente).
- Los mensajes flash se guardan en sesión: si se abre el listado en varias pestañas,
  el mensaje puede aparecer en cualquiera de ellas.
