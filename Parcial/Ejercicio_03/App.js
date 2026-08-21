import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View } from 'react-native';
import ImagenFondo from './componentes/ImagenFondo';
import DemoFlatList from './componentes/DemoFlatList'
import DemoSectionList from './componentes/DemoSectionList'

export default function App() {
  return (
    <View style={styles.container}>
      <DemoSectionList/>
      <DemoFlatList/>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
