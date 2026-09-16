import { StyleSheet, View } from 'react-native';
import PedometerSensor from './components/PedometerSensor';

export default function App() {
  return (
    <View style={styles.container}>
      <PedometerSensor />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#dff6ff',
  },
});
