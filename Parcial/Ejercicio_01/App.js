import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View, Image } from 'react-native';
import MiComponente  from './componentes/MiComponente';
import Mensaje from './componentes/Mensaje';

//Cada componente es un módulo en JS
export default function App() {

  //Lógica del componente, si necesito una función puedo invocarla aquí.
  return (
    //El retorno de este componente siempre tiene que estar en View, es como el fragmento principal.
    <View style={styles.container}>
      <Text style={styles.texto}> I'M </Text>
      <MiComponente/>
      <Image 
        source={{ uri: 'https://i.redd.it/xnha2jvpo2eh1.jpeg' }} 
        style={styles.networkImage} 
      />
      <Mensaje titulo="KING OF MYCENAE" numero="≋≋"/>
      <Mensaje titulo="AND THE GREEKS"/>
      <StatusBar style="auto" />
    </View>
  );
}

//Aquí siempre serán los estilos, que es una instancia de la clase StyleSheet.

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000000',
    alignItems: 'center',
    justifyContent: 'center',
  },
  networkImage: {
    width: 200,
    height: 400,
  },
  texto: {
    color: '#fff',
    fontSize: 15,
  },
});
