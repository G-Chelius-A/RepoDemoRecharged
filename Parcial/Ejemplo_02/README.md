# Comando de creación:

npx create-expo-app@latest Ejemplo_02 --template blank
npx expo install react-dom react-native-web

# Correr:
npm run web //para web


# Tunelización (diferentes redes).
npm install --save-dev @expo/ngrok@^4.1.0   (Instala NGrok local, para desarrollo).

npx expo start --tunnel


# Ejemplo de uso de Child
## DemoChildren.jsx
import { View, Text } from 'react-native';

export default function DemoChildren({children, titulo}){
    return(
    <View>
        <Text>{titulo}</Text>
        <Text>Muestra Child: </Text> {children}
    </View>
    );
}

## App.js
import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View } from 'react-native';
import DemoChildren from './componentes/DemoChildren';

export default function App() {
  return (
    <View style={styles.container}>
      <Text>Open up App.js to start working on your app!</Text>
      <DemoChildren titulo="Ejemplo de uso de children"> Esto es el Child <Text>Mensaje en el Child</Text></DemoChildren>
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});



# Vamos a ver
Botón y text input