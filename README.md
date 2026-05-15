# Gestiona Tus Tareas

Aplicación Android desarrollada en Java para organizar tareas personales, académicas y compartidas. Permite crear una cuenta, iniciar sesión y guardar tareas con fecha límite, prioridad, categoría, estado y recordatorio.

La aplicación también incluye colaboración entre usuarios. Un usuario puede enviar una petición para compartir una tarea, aceptar o rechazar peticiones recibidas y usar un chat dentro de una tarea compartida.

## Funciones principales

- Registro e inicio de sesión de usuarios.
- Cierre de sesión.
- Crear, editar y eliminar tareas.
- Confirmación antes de borrar una tarea.
- Marcar tareas como completadas.
- Estados de tarea: pendiente, en progreso y completada.
- Prioridades: baja, media y alta.
- Categorías para organizar las tareas.
- Ordenación por fecha, estado o categoría.
- Calendario para consultar tareas por día.
- Recordatorios con aviso al usuario.
- Peticiones de colaboración entre usuarios.
- Opción de aceptar o rechazar peticiones.
- Chat dentro de tareas compartidas.
- Colores visuales para diferenciar el estado de las tareas.

## Tecnologías utilizadas

- Java.
- Android Studio.
- Firebase Authentication.
- Firebase Realtime Database.
- Gradle.
- AlarmManager.
- Arquitectura MVC.

## Organización del proyecto

El código está separado por carpetas siguiendo una estructura MVC sencilla:

- `controlador`: contiene las pantallas principales de la aplicación.
- `modelo`: contiene las clases de datos de la app.
- `modelo/repositorio`: contiene las clases encargadas de trabajar con Firebase.
- `modelo/resultado`: contiene interfaces usadas para recibir respuestas de las operaciones.
- `modelo/utilidad`: contiene clases de apoyo para fechas, textos y recordatorios.
- `vista`: contiene el adaptador que muestra las tareas en pantalla.
- `res/layout`: contiene los diseños XML de las pantallas.
- `res/drawable`: contiene fondos, estilos y recursos visuales.
- `docs`: contiene la documentación y los diagramas del proyecto.

## Base de datos

La aplicación usa Firebase Realtime Database. Los datos principales se guardan en estos nodos:

- `usuarios`: datos básicos de cada usuario.
- `tareas`: tareas guardadas por usuario.
- `peticiones`: solicitudes para colaborar en tareas.
- `chats`: mensajes de las tareas compartidas.

Ejemplo simplificado:

```json
{
  "usuarios": {
    "uid_usuario": {
      "id": "uid_usuario",
      "nombre": "Jose",
      "correo": "jose@gmail.com"
    }
  },
  "tareas": {
    "uid_usuario": {
      "id_tarea": {
        "titulo": "Examen",
        "descripcion": "Estudiar base de datos",
        "prioridad": "ALTA",
        "estado": "PENDIENTE",
        "fechaLimite": "2026-06-02",
        "categoriaId": "Estudios",
        "usuarioId": "uid_usuario"
      }
    }
  }
}
```

## Cómo ejecutar el proyecto

1. Abrir el proyecto con Android Studio.
2. Esperar a que Gradle sincronice las dependencias.
3. Configurar Firebase Authentication y Firebase Realtime Database.
4. Añadir el archivo `google-services.json` dentro de la carpeta `app/`.
5. Ejecutar la aplicación en un emulador o en un móvil Android.

## Permisos

La aplicación utiliza estos permisos:

- `INTERNET`, para conectarse con Firebase.
- `POST_NOTIFICATIONS`, para mostrar avisos y recordatorios en Android.

## Documentación

La documentación final está en:

`docs/Documentacion/Documentacion-Completa.pdf`

Incluye la definición del proyecto, análisis de mercado, requisitos, especificaciones técnicas, viabilidad, planificación, análisis, diseño, diagramas y mockups.

## Estado del proyecto

El proyecto tiene implementada una primera versión funcional con autenticación, gestión de tareas, calendario, recordatorios, colaboración y chat. Como mejoras futuras se podrían añadir un buscador de tareas, categorías personalizadas, modo sin conexión, estadísticas y la posibilidad de compartir una tarea con más de un usuario.

## Autor

Jose Antonio Gómez Campaña.
