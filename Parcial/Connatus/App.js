import React, { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { StyleSheet, View } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import SplashScreen from './src/components/SplashScreen';
import TabNavigator from './src/navigation/TabNavigator';
import { COLORS } from './src/theme/theme';

export default function App() {
  const [showSplash, setShowSplash] = useState(true);

  if (showSplash) {
    return (
      <SafeAreaProvider>
        <View style={styles.container}>
          <StatusBar style="dark" translucent={true} backgroundColor="transparent" />
          <SplashScreen onFinish={() => setShowSplash(false)} />
        </View>
      </SafeAreaProvider>
    );
  }

  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <StatusBar style="dark" translucent={true} backgroundColor="transparent" />
        <TabNavigator />
      </NavigationContainer>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.paper,
  },
});
