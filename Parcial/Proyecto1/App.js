import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View, Image, Button, Alert } from 'react-native';

export default function App() {
  return (
    <View style={styles.container}>
      <Text>Open up App.js to start working on your app!</Text>
      <Text> HOY REVIVE MI REPO DEMO REMOTO</Text>
      <Image 
        source={{ uri: 'https://cdn.pixabay.com/photo/2015/06/18/21/08/cat-814141_640.jpg' }} 
        style={styles.networkImage} 
      />
     <Button
        title="Press Me"
        color="#841584"
        onPress={() => Alert.alert('Button pressed')}
      />
      <StatusBar style="auto" />
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
  networkImage: {
    width: 100,
    height: 100,
  },
});
