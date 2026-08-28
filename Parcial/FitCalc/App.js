import { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View, SafeAreaView, TextInput, Button, Image } from 'react-native';
import CustomModal from "./components/CustomModal";

export default function App() {

  const [p, setPeso] = useState(1)
  const [a, setAltura] = useState(1)
  const [modalVisible, setModalVisible] = useState(false);
  const objetoContenido = (p/(a**2)).toFixed(2);
  const objetoContenido2 = asignaNivel(objetoContenido);

  return (
    <SafeAreaView style={styles.container}>
      <View>

        <Image
            source={{uri:'https://thefit24.com/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fcouple-fit.d6aa6362.png&w=1920&q=75'}}
            style={styles.foto}
        />
        <Text style={styles.title}>FitCalc</Text>
        <CustomModal
            visible={modalVisible}
            onClose={()=>setModalVisible(false)}
            contenido={objetoContenido}
            contenido2={objetoContenido2}
        />
       <Text>Peso (kg)</Text>
        <TextInput
            placeholder="Peso"
            onChangeText={p=>setPeso(p)}  
        />

        <Text>Altura (m)</Text>
        <TextInput
            placeholder="Altura"
            onChangeText={a=>setAltura(a)}  
        />

        <Button
          title="Calcula IMC"
          onPress={() => {
          setModalVisible(true);
          }}
        />
      </View>

    </SafeAreaView>
  );
}

function asignaNivel(imc){
  if(imc<18.49){
    return "Bajo";
  } else {
    if(imc<24.99){
      return "Peso normal";
    } else {
      if(imc<29.99){
        return "Sobrepeso";
      } else {
        if(imc<34.99){
          return "Obesidad leve";
        } else {
          if(imc<39.99){
            return "Obesidad media";
          } else {
            return "Obesidad mórbida";
          }
        }
      }
    }
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    padding: 40,
    fontWeight: 'bold',
    fontSize: 40,
  },
  foto: {
    width: 200,
    height: 250,
  },
});
