import { useState } from "react";
import { StyleSheet, Text, View, Image, TextInput, Button, Dimensions, ScrollView } from 'react-native';
import FlagComponent from './componentes/FlagComponent';

export default function App() {

    const [texto, setTexto] = useState()
    const [enviar, setEnviar] = useState()

  return (
    <View style={styles.container}>
      <View style={styles.view1}>
        <Image
          source={{uri: 'https://flags.paxhistoria.co/revolutionary_monkeys_army_rma_1991-2006_fictional-1991-2006.png' }}
          style={styles.networkImage} 

        />
      </View>
      <View style={styles.view2}>
        <ScrollView style={styles.input}>
           <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>

        </ScrollView>
      </View>
      <View style={styles.view3}>
        <TextInput
          placeholder="Escribe aquí..."
          onChangeText={t=>setTexto(t)}
        />
        <Button 
          title="Enviar"
          onPress={()=>setEnviar(texto)}
        />


      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    backgroundColor: '#42c029',

  },
  view1: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#ff0d0d',
  },
  view2: {
    flex: 1,
    backgroundColor: '#ffee03',
  },
  view3: {
    flex: 1,
    backgroundColor: '#2ad300',
  },
  input: {
    width: Dimensions.get("window").width,
  },
  networkImage: { 
    width: 250, 
    height: 180,
  },
});
