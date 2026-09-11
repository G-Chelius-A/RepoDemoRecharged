# Compilar en APk

Descargar EAS: npm install -g eas-cli
Descargar EAS: npm install eas

Login a EAS: eas login

Compilar el proyecto: eas build:configure

Se genera un archivo .json. Puedes editarlo con eas.json
Genera un .aab con eas build -p android

# Compilación y distribución
## Android
.apk -> Android Package (Es cuando yo quiero generar la app en un sólo paquete y ocupa mucho espacio)
.aab -> Android App Bundle (Este se sube a la PlayStore)

## iOS

.app -> iOS App Bundle (Equivalente al .aab)
.ipa -> iOS App Archive (Equivalente al .apk, se genera en el Apple Store)

# BundleTool (APK)

Descargar ek .jar de: https://github.com/google/bundletool/releases

java -jar bundletool-all-1.18.3.jar build-apks --bundle=VidaStation.aab --output=resultado.apks --mode=universal  (Java 17 o superior)

# Builds con gradlew
npm expo android
Genera una carpeta con gradlew
assembleRelease

npx prebuild android
npx expo prebuild --platform android

Conversación en el Modo IA: como meter java en el pathDijiste: como meter java en el pathcomo meter java en el pathPara agregar Java al PATH en Windows, debes configurar las variables de entorno desde la configuración del sistema. Sigue estos pasos sencillos para hacerlo:Pasos en WindowsAbre el menú Inicio y escribe variables de entorno.Haz clic en la opción Editar las variables de entorno del sistema.En la ventana que aparece, presiona el botón Variables de entorno... abajo a la derecha.En la sección Variables del sistema, busca la variable llamada Path y selecciónala.Haz clic en el botón Editar....Haz clic en Nuevo y añade la ruta exacta de la carpeta bin de tu instalación de Java (por ejemplo: C:\Program Files\Java\jdk-21\bin).Opcional recomendado: Crea primero una variable llamada JAVA_HOME con la ruta principal del JDK (sin incluir bin), y en el Path agrega %JAVA_HOME%\bin.Haz clic en Aceptar en todas las ventanas abiertas para guardar los cambios.Verificar la instalaciónAbre una nueva ventana de Símbolo del sistema (CMD) o PowerShell.Escribe el comando para:bashjava -version
Usa el código con precaución.Si está bien configurado, verás la versión de Java instalada en tu equipo.Puedes ver una guía visual de este proceso en el siguiente video explicativo:3:05How to install Java JDK on Windows 10/11 and configure the ...31 k reproducciones · hace 4 añosYouTube · Randy Varela¿Estás realizando esta configuración en Windows, macOS o Linux, y qué versión de Java tienes instalada?Java¿Cómo puedo establecer o cambiar la variable del sistema ...Para ver si la ruta está bien definida: en una ventana de terminal, introduzca: % java -version. De esta forma, se imprimirá la ve...DEV CommunityCómo instalar y configurar la variable PATH para JavaTraducido — Paso 1: Descargar Java. ... Seleccione el kit de desarrollo de Java (JDK) más reciente [JDK 25] LTS para Windows. Descarga el inst...YouTube·Sotoboteroinstalar java en Windows y configurar el java_homeinstalar java en Windows y configurar el java_home okay y vemos que aquí se nos ha agregado la variable Java Home ya con eso todas...2mComando NPX prebuild anrdoidEl comando correcto para generar la carpeta nativa de Android en un proyecto de Expo (React Native) es npx expo prebuild --platform android.Comandos útiles para PrebuildSolo Android:bashnpx expo prebuild --platform android
Usa el código con precaución.Limpiar y regenerar desde cero (elimina la carpeta android previa y la recrea):bashnpx expo prebuild --platform android --clean
Usa el código con precaución.Ambas plataformas (iOS y Android):bashnpx expo prebuild
Usa el código con precaución.Requisitos importantesComo acabas de configurar Java en tu PATH, asegúrate de haber cerrado y vuelto a abrir tu terminal (o VS Code) para que reconozca los cambios. Además, para que este comando funcione correctamente, necesitas tener configurada la variable ANDROID_HOME apuntando a tu Android SDK.¿Te ha surgido algún error específico de Gradle o Java al ejecutar el comando?C:\Users\SOPORTE\Downloads>java -jar bundletool-all-1.18.3.jar build-apks --bundle=MyApp.aab --output=hola.apks --mode=universal
WARNING: The APKs won't be signed and thus not installable unless you also pass a keystore via the flag --ks. See the command help for more information.El mensaje que estás viendo no es un error, sino una advertencia (WARNING). Significa que bundletool generó con éxito tu archivo hola.apks, pero no podrás instalarlo en ningún teléfono porque no está firmado digitalmente.Si solo quieres inspeccionar el contenido del .apks, puedes ignorar el mensaje. Pero si tu objetivo es instalar ese APK universal en un dispositivo, debes firmarlo agregando tu archivo de claves (.keystore o .jks).Aquí tienes cómo generar la clave y cómo usarla en tu comando:1. Generar un Keystore de prueba (Si no tienes uno)Si no tienes una clave de firma, crea una rápida desde tu terminal usando este comando (debes tener Java en tu PATH para usar keytool):bashkeytool -genkey -v -keystore mi-clave.keystore -alias mi-alias -keyalg RSA -keysize 2048 -validity 10000
Usa el código con precaución.Te pedirá crear una contraseña (recuérdala) y rellenar algunos datos.2. Comando completo para generar e instalar el APK firmadoUna vez que tengas tu archivo .keystore, añade las banderas correspondientes a tu comando de bundletool:bashjava -jar bundletool-all-1.18.3.jar build-apks --bundle=MyApp.aab --output=hola.apks --mode=universal --ks=mi-clave.keystore --ks-key-alias=mi-alias
Usa el código con precaución.Al ejecutarlo, te solicitará la contraseña que le asignaste a tu almacén de claves.3. ¿Cómo instalarlo en tu celular?Cuando el comando termine sin la advertencia, puedes conectar tu teléfono Android por USB (con la Depuración USB activa) e instalar el archivo directamente ejecutando:bashjava -jar bundletool-all-1.18.3.jar install-apks --apks=hola.apks
Usa el código con precaución.¿Tienes ya una clave de producción (.jks / .keystore) generada por Android Studio o Expo, o estás usando una de prueba?C:\Users\SOPORTE\Downloads>keytool -genkey -v -keystore mi-clave.keystore -alias mi-alias -keyalg RSA -keysize
"keytool" no se reconoce como un comando interno o externo,
programa o archivo por lotes ejecutable. pero Java si está en el PATHEl error ocurre porque añadiste directamente el archivo java.exe al PATH o tu variable apunta a una carpeta incorrecta, en lugar de apuntar a la carpeta bin completa donde residen tanto java como keytool.Dado que keytool se encuentra exactamente en la misma carpeta que java, resolveremos esto rápidamente con dos opciones:Solución rápida (Sin editar el PATH)Si tienes prisa, puedes ejecutar keytool usando la ruta absoluta de tu instalación de Java. Ejecuta este comando en tu terminal (ajusta el número de versión jdk-21 si la tuya es diferente):cmd"C:\Program Files\Java\jdk-21\bin\keytool.exe" -genkey -v -keystore mi-clave.keystore -alias mi-alias -keyalg RSA -keysize 2048 -validity 10000
Usa el código con precaución.Solución definitiva (Corregir el PATH)Para arreglarlo permanentemente y que el comando funcione desde cualquier lado, revisa cómo quedó tu variable de entorno:Abre de nuevo las Variables de entorno.En las Variables del sistema, edita la variable Path.Revisa la línea que pusiste de Java:❌ Incorrecto: C:\Program Files\Java\jdk-21\bin\java.exe (No debes poner el archivo .exe).❌ Incorrecto: C:\Program Files\Java\jdk-21 (Falta la carpeta bin).Correcto: C:\Program Files\Java\jdk-21\bin (Debe terminar estrictamente en \bin).Guarda los cambios, cierra por completo esa ventana de CMD y abre una nueva para que se aplique la configuración.¿Pudiste generar el archivo .keystore usando la ruta absoluta o prefieres que verifiquemos en qué carpeta exacta se instaló tu Java?