import { useState } from "react";
import { StyleSheet, View, Button, SafeAreaView, TextInput } from 'react-native';
import CustomModal from "./components/CustomModal";

export default function App() {

    const [modalVisible, setModalVisible] = useState(false);
    const [text, setText] = useState('');
    const objetoContenido = {valor: text};

    /* Safe Area View es como un padding*/

  return (
    <SafeAreaView style={styles.container}>
        <View>
            <CustomModal
                visible={modalVisible}
                onClose={()=>setModalVisible(false)}
                contenido={objetoContenido}
            />

            <Button
                title="Abrir modal"
              onPress={() => {
                setModalVisible(true);
              }}
            />

            {/* Aquí, agregar un input text. */}

            <TextInput 
            placeholder="Ingresa tu nombre"
            onChangeText={t => setText(t)}
            />

        </View>
    </SafeAreaView>
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
