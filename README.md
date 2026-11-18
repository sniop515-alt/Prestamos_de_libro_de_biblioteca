# Sistema de biblioteca en Java con MySQL

Aplicación de escritorio en Java (Swing + JDBC) para registrar préstamos y devoluciones de libros. Usa una base de datos MySQL con 6 tablas relacionadas.

## Tablas

| Tabla | Descripción |
| --- | --- |
| `socio` | Personas que piden libros prestados |
| `bibliotecario` | Empleados que registran los préstamos |
| `autor` | Autores de los libros |
| `libro` | Libros con su autor y ejemplares disponibles |
| `prestamo` | Cabecera del préstamo (fechas, estado, socio, bibliotecario) |
| `detalle_prestamo` | Libros de cada préstamo y si ya fueron devueltos |

## Pantallas

- **Socios:** guardar, actualizar y eliminar.
- **Nuevo préstamo:** elegir socio y bibliotecario, agregar hasta 3 libros, definir los días y descontar los ejemplares disponibles (en una transacción).
- **Préstamos y devoluciones:** lista con filtro, marca los préstamos atrasados y registra la devolución de cada libro (devuelve el ejemplar al stock).

## Cómo ejecutarlo

1. En phpMyAdmin, importar `schema.sql` (crea la base `biblioteca`, las tablas y datos de ejemplo).
2. En NetBeans: File → Open Project y elegir la carpeta `Biblioteca`.
3. Si tu MySQL tiene contraseña, escribirla en `Config.java` (constante `CLAVE`). Es el único lugar. Con XAMPP suele estar vacía.
4. Ejecutar la clase `Principal`.

## Archivos

- `schema.sql`: script de la base de datos
- `Config.java`: datos de conexión
- `accesobd.java`: conexión JDBC
- `SocioBeans.java`: datos y operaciones del socio
- `FrmSocios.java`, `FrmPrestamo.java`, `FrmPrestamos.java`: formularios
- `Principal.java`: menú principal
- `pom.xml`: configuración de Maven
