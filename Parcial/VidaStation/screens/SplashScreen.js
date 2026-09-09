import React, { useEffect } from 'react';
import { View, StyleSheet } from 'react-native';
import Logo from '../components/Logo';

export default function SplashScreen({ navigation }) {
  useEffect(() => {
    const timer = setTimeout(() => {
      if (navigation && typeof navigation.replace === 'function') {
        navigation.replace('Home');
        return;
      }

      if (navigation && typeof navigation.navigate === 'function') {
        navigation.navigate('Home');
      }
    }, 2200);

    return () => clearTimeout(timer);
  }, [navigation]);

  return (
    <View style={styles.container}>
      <View style={styles.glow} />
      <Logo size="large" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#edf4ff'
  },
  glow: {
    position: 'absolute',
    width: 240,
    height: 240,
    borderRadius: 120,
    backgroundColor: 'rgba(125, 211, 252, 0.24)',
    shadowColor: '#60a5fa',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.6,
    shadowRadius: 40,
    elevation: 8
  }
});
