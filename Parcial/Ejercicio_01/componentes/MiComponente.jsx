import { View, Text, StyleSheet } from "react-native";

const MiComponente = () =>{
    return(
        <View>
            <Text style={styles.color_texto}>
                AGAMEMNON
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    color_texto:{
        color: 'red',
        fontSize: 32,
        fontWeight: 'bold',
    },
});

export default MiComponente;