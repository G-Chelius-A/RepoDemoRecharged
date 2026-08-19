import { View, StyleSheet, Text } from "react-native";

const FlagComponent = () => {
    return(
        <View style={styles.container}>
            <Text>Hola</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#0d5c0d',
        alignItems: 'center',
        justifyContent: 'center',
    },
});

export default FlagComponent;