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

> **Acerca de este repositorio:** GitHub aloja **únicamente el código fuente y la
> configuración** del proyecto. Ni la base de datos ni el servidor de aplicaciones
> viven en GitHub ni en la nube: PostgreSQL corre localmente en Docker (contenedor
> `mercadonexo-db`) y **GlassFish se instala localmente** (carpeta `glassfish7/`,
> ignorada por git y no incluida en el repositorio). Cualquiera que clone este repo
> puede reproducir el entorno completo con los pasos de esta sección.

### 4.0 Requisitos previos y verificación

| Requisito | Versión | Verificación |
|---|---|---|
| JDK | **Java 21** | `java -version` |
| Maven | **3.9 o superior** | `mvn -version` |
| Docker + Compose | versión reciente con subcomando `docker compose` | `docker --version` · `docker compose version` |
| GlassFish | **7.0.25** (se descarga e instala en el paso 4.5) | se verifica al usar `asadmin` en el paso 4.6 |

En una terminal abierta en la carpeta del proyecto, ejecuta (los mismos comandos en
Windows y Linux):

```bash
java -version            # debe reportar la versión 21
mvn -version             # debe reportar Maven 3.9+ y Java 21
docker --version
docker compose version   # debe reportar una versión de Docker Compose v2
```

Si falta alguno: instala **OpenJDK 21** (por ejemplo, Eclipse Temurin), **Maven 3.9+**
y **Docker Desktop** (Windows) o Docker Engine + plugin compose (Linux).

> **Nota por sistema operativo:**
> - **Windows:** abre **PowerShell** y navega a la carpeta del proyecto, por ejemplo
>   `cd "C:\Users\TU_USUARIO\Documents\mercado-nexo"`.
> - **Linux:** abre Bash y navega con `cd /ruta/a/mercado-nexo`.

### 4.1 Obtener el proyecto

```bash
git clone https://github.com/fernando22913/mercado-nexo.git
cd mercado-nexo
```

Alternativa sin Git: en GitHub, botón **Code → Download ZIP**, descomprimir el
archivo y abrir la terminal en la carpeta descomprimida. Todos los comandos
siguientes asumen que la terminal está en la **raíz del proyecto** (la carpeta que
contiene `pom.xml` y `docker-compose.yml`).

### 4.2 Configurar `.env` (base de datos)

```bash
# Linux (Bash)
cp .env.example .env
```

```powershell
# Windows (PowerShell)
Copy-Item .env.example .env
```

`.env.example` define los valores con los que Docker creará PostgreSQL:

```properties
POSTGRES_DB=mercadonexo
POSTGRES_USER=mercadonexo
POSTGRES_PASSWORD=mercadonexo
POSTGRES_PORT=5432
```

Con los valores por defecto **no hay que editar nada**: ya coinciden con la conexión
configurada en `src/main/resources/META-INF/persistence.xml`.

> **Importante — `.env` y `persistence.xml` deben coincidir:**
> - `.env` configura **solamente el contenedor Docker de PostgreSQL**: nombre de la
>   base de datos, usuario, contraseña y puerto publicado (los lee `docker-compose.yml`).
> - `persistence.xml` configura **solamente la conexión JDBC de la aplicación Java**:
>   URL `jdbc:postgresql://localhost:5432/mercadonexo`, usuario `mercadonexo` y
>   contraseña `mercadonexo`.
> - Si cambias un valor en uno, cámbialo también en el otro. Ejemplo: si en `.env`
>   pones `POSTGRES_PORT=5433`, la URL en `persistence.xml` debe pasar a
>   `jdbc:postgresql://localhost:5433/mercadonexo`.
> - El usuario y la contraseña de PostgreSQL solo se fijan al **crear el contenedor
>   la primera vez**; cambiarlos más adelante requiere recrear el contenedor
>   (`docker compose down -v`, lo que borra los datos).

### 4.3 Iniciar PostgreSQL y verificar su estado

```bash
docker compose up -d     # crea e inicia el contenedor mercadonexo-db
docker compose ps        # STATUS debe mostrar Up ... (healthy)
```

El `docker-compose.yml` define un healthcheck (`pg_isready`), así que al cabo de unos
segundos `docker compose ps` debe mostrar el estado **healthy**. Verificación
adicional de conexión real (idéntica en Windows y Linux):

```bash
docker compose exec db psql -U mercadonexo -d mercadonexo -c "\dt"
# Debe terminar sin error. Si aún no se desplegó la aplicación puede decir
# "Did not find any relations": es normal, las tablas se crean al desplegar (§3).
```

> Si otro servicio local ya usa el puerto 5432, ver la nota correspondiente en §8.

### 4.4 Compilar el WAR y ejecutar las pruebas

```bash
mvn package              # compila + empaqueta target/mercado-nexo.war
mvn test                 # 14 pruebas JUnit 5 contra la BD real (requiere PostgreSQL iniciado, §4.3)
```

Ambos comandos son iguales en Windows y Linux. `mvn test` conecta directamente a la
base de datos, por eso PostgreSQL debe estar iniciado antes de ejecutarlo. Para
compilar sin ejecutar pruebas: `mvn package -DskipTests`.

### 4.5 Instalar GlassFish 7.0.25 (una sola vez)

GlassFish **no viene con el proyecto ni está en el repositorio**: se descarga el ZIP
oficial 7.0.25 y se descomprime en la raíz del proyecto, lo que crea la carpeta
`glassfish7/` (ignorada por git). El ZIP contiene la carpeta `glassfish7/` en su raíz,
por lo que basta descomprimirlo ahí.

**Linux (Bash):**

```bash
curl -fLO https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/7.0.25/glassfish-7.0.25.zip
unzip glassfish-7.0.25.zip -d .        # crea glassfish7/
```

**Windows (PowerShell):**

```powershell
curl.exe -fLO https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/7.0.25/glassfish-7.0.25.zip
Expand-Archive glassfish-7.0.25.zip -DestinationPath .    # crea glassfish7\
```

El ZIP descargado puede borrarse después de descomprimirlo.

### 4.6 Arrancar GlassFish y desplegar la aplicación

La única diferencia entre sistemas operativos es el separador de ruta del ejecutable
`asadmin` (`/` en Linux, `\` en Windows):

```bash
# Linux (Bash)
glassfish7/bin/asadmin start-domain    # arranca el dominio: http://localhost:8080 · consola admin 4848
glassfish7/bin/asadmin deploy target/mercado-nexo.war
```

```powershell
# Windows (PowerShell)
.\glassfish7\bin\asadmin start-domain
.\glassfish7\bin\asadmin deploy target\mercado-nexo.war
```

El primer arranque del dominio puede tardar un momento. `deploy` copia el WAR al
dominio y registra el despliegue con el nombre **mercado-nexo** (el nombre del WAR).

### 4.7 Usar la aplicación

- **Listado:** http://localhost:8080/mercado-nexo/productos
- Crear → botón “+ Nuevo producto”; Editar → botón “Editar”; Detalle → “Ver”;
  Eliminar → “Eliminar” (pide confirmación).
- **Operaciones masivas (JPQL):** panel en el listado — ajuste de precios por
  categoría (UPDATE masivo) y eliminación de productos sin stock (DELETE masivo).
- **Categorías:** http://localhost:8080/mercado-nexo/categorias

### 4.8 Detener, reiniciar y re-desplegar (sin perder datos)

**Base de datos.** Los datos viven en el volumen Docker nombrado `mercadonexo_pgdata`,
que `stop` y `down` nunca borran:

```bash
docker compose stop db      # detener el contenedor (volumen intacto)
docker compose start db     # retomar con todos los datos
# o bien: docker compose down && docker compose up -d   (mismo volumen, datos intactos)
```

> `docker compose down -v` **borra el volumen y todos los datos** (reset total):
> usarlo solo cuando se quiera empezar de cero.

**GlassFish** (mismos comandos en ambos SO; solo cambia el separador de ruta):

```bash
glassfish7/bin/asadmin stop-domain     # Linux ·  Windows: .\glassfish7\bin\asadmin stop-domain
glassfish7/bin/asadmin start-domain    # volver a arrancar
```

Si al arrancar de nuevo la aplicación no responde en el navegador, re-despliégala
según el procedimiento de abajo.

**Re-desplegar tras recompilar.** Al recompilar (`mvn package -DskipTests`) se genera
un WAR nuevo en `target/`, pero **arrancar o reiniciar el dominio no instala ese WAR
nuevo por sí solo**: hay que volver a ejecutar `deploy`. Para actualizar la
aplicación ya desplegada se recomienda quitar el despliegue anterior, reiniciar el
dominio y desplegar el WAR nuevo — en lugar de usar `deploy --force`, que puede
provocar conflictos de classloaders con la unidad de persistencia registrada a nivel
de servidor:

```bash
# 1) Recompilar:                    mvn package -DskipTests
# 2) Quitar el despliegue anterior (con el dominio corriendo):
glassfish7/bin/asadmin undeploy mercado-nexo
# 3) Reiniciar el dominio:
glassfish7/bin/asadmin stop-domain && glassfish7/bin/asadmin start-domain
# 4) Desplegar el WAR nuevo:
glassfish7/bin/asadmin deploy target/mercado-nexo.war
```

(En Windows: `.\glassfish7\bin\asadmin ...` y `target\mercado-nexo.war`.)

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
