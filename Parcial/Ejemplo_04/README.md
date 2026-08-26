# Comando de creación:

npx create-expo-app@latest Ejemplo_02 --template blank
npx expo install react-dom react-native-web

# Correr:
npm run web //para web


# Tunelización (diferentes redes).
npm install --save-dev @expo/ngrok@^4.1.0   (Instala NGrok local, para desarrollo).

npx expo start --tunnel

# Hoy vamos a trabajar "Modal"

El modal va a ser una "ventana" que se va a posicionar en lo que ya tenemos; por tanto, es un view que se puede hacer transparente para que se pueda ver lo de abajo. Por tanto, yo necesito otro view para ver lo que queremos mostrar.

Es un view y otro view adentro.