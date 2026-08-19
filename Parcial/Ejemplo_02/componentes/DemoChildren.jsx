import { View, Text } from 'react-native';

export default function DemoChildren({children, titulo}){
    return(
    <View>
        <Text>{titulo}</Text>
        <Text>Muestra Child: </Text> {children}
    </View>
    );
}
