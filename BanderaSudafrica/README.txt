BANDERA DE SUDÁFRICA - Java + JOGL
==================================
Requisitos: JDK 11 o superior y Maven (descarga JOGL 2.5.0 automáticamente).

Ejecutar (la primera vez fuerza la descarga):
    mvn -U compile exec:java

Sin Maven (Eclipse / NetBeans / IntelliJ):
    1. Crear un proyecto Java y copiar src/flag/BanderaSudafrica.java
    2. Agregar al classpath: jogl-all.jar y gluegen-rt.jar
       (más los jar "natives" de tu sistema operativo)
       https://jogamp.org/deployment/jogamp-current/archive/jogamp-all-platforms.7z
    3. Ejecutar la clase flag.BanderaSudafrica

Colores oficiales: rojo #E03C31, azul #001489, verde #007749,
amarillo #FFB81C, blanco y negro. Proporción 3:2.

Si falla con "POM ... is missing": borra la carpeta
~/.m2/repository/org/jogamp  (C:\Users\TU_USUARIO\.m2\repository\org\jogamp en Windows)
y vuelve a ejecutar con -U. JOGL 2.5.0 se descarga de https://jogamp.org/deployment/maven
