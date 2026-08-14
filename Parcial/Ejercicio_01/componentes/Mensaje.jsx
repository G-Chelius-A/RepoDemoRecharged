import { View, Text, StyleSheet } from "react-native";

const miTexto="KING OF MYCENAE";

/* const double = (n) => {
    return 2*n;
}
*/

// const double = n => n*2

export default function Mensaje ( props ){
    return(
        <View>
            <Text style={styles.color_texto}>
                { props.titulo
                //miTexto 
                //+ " " + num 
                // + double(3)
                }
            </Text>
            <Text style={styles.color_texto}>
                {props.numero}
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    color_texto:{
        color: 'white',
        fontSize: 21,
        textAlign: 'center',
        fontWeight: 'bold',
        textShadowColor: 'rgb(255, 52, 52)',
        textShadowOffset: { width: 0, height: 3.5 },
        textShadowRadius: 5,
        //backgroundColor: 'yellow',
    },
});