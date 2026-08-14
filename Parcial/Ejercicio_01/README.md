# Comando de creación:

npx create-expo-app@latest Ejercicio_01 --template blank
npx expo install react-dom react-native-web

# Correr:
npm run web //para web


# Tunelización (diferentes redes).
npm install --save-dev @expo/ngrok@^4.1.0   (Instala NGrok local, para desarrollo).

npx expo start --tunnel

# Notas del proyecto

Hay una carpeta llamada node_modules, que tiene todos los módulos para que funcione la aplicación (como en web). .gitignore.
Componente principal: App.js
Configuración de la aplicacón: app.json

Relación de las dependencias: package-lock.json.

Revisar imagen de traducción de iOS y Android: https://reactnative.dev/docs/intro-react-native-components 

Y la tabla también


Pasar las propiedades