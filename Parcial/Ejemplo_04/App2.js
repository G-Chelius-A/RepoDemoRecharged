import { StatusBar } from 'expo-status-bar';
import { useState } from 'react';
import { StyleSheet, Text, View, Modal, Button } from 'react-native';
//Uso del Modal

export default function App2() {

    const [modal, setModal] = useState(false);

  return (
    <View style={styles.container}>
      <Modal
        //Configuración del Modal.
        //Slide, fade y none.
        animationType='fade'
        transparent={true}
        visible={modal}
      >
         {/** Aquí va el contenido del modal **/}
          <View style={styles.containerModal}>
            <View style={styles.viewModal}>
              <Text> Esto está dentro del Modal </Text>
                <Button
                  title="Cerrar modal"
                  onPress={()=>setModal(!modal)}
                />
            </View>
          </View>
      </Modal>
      <Text>Esto está fuera del Modal</Text>
      <Button
      title = "Mostrar Modal"
      onPress={()=>{ setModal(!modal)}}
      />
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
  containerModal: {
    flex: 1,
    alginItems: 'stretch',
    justifyContent: 'center',
    backgroundColor: 'rgba(0,0,0,0.5)'
  }, 
  viewModal: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
    marginHorizontal: '20%',
    marginVertical: '90%',

  }
});
