import { useState } from "react";
import { View, Text, TextInput, Button, ScrollView, StyleSheet, Image, Dimensions } from "react-native";

export default function MyInputText(){

    const [texto, setTexto] = useState()
    const [enviar, setEnviar] = useState()

    return(
        <View>
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

                <TextInput
                    placeholder="Escribe aquí..."
                    onChangeText={t=>setTexto(t)}
                />
                <Button 
                    title="Enviar"
                    onPress={()=>setEnviar(texto)}
                />

                <Image 
                    source={{uri:'https://www.infobae.com/resizer/v2/GKLKB6CAU5DX3D6VZK4WN2ISNI.jpg?auth=5b8d3d1a6619be3342fcdd09e63621c8645449f0779abf1badd8de289d708f34'}}
                    style={styles.networkImage} 
                />
            </ScrollView>
        </View>
    );
}

const styles = StyleSheet.create({
  input: {
    width: Dimensions.get("window").width,
  },
  networkImage: { 
    width: 250, 
    height: 250,
  },
});
